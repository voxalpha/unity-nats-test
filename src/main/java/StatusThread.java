import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

public class StatusThread implements Runnable{

    private Collection<NatsStatus> clients = new ArrayList<>();
    private int timeout = 10000;
    private boolean isTerminated;

    public void addClient(NatsStatus statistics){
        clients.add(statistics);
    }

    public void print(){
        if (clients.isEmpty()) {return;}

        if (!clients.stream().anyMatch(c -> c.updated))
        {
            System.out.print(".");
        }
        else {
            System.out.println();
            System.out.println("========================");
        }

        NumberFormat nf = NumberFormat.getIntegerInstance();
        for (NatsStatus s: clients) {

            if (!s.updated) continue;
            long timePassed;
            if (s.inProgress)
               timePassed = (System.nanoTime() - s.batchStartedNanoTime) + s.activeNanoTime.longValue();
               // timePassed = Long.MAX_VALUE;
            else
               //timePassed = s.batchFinishedNanoTime - s.batchStartedNanoTime;
               timePassed = s.activeNanoTime.longValue();
               //timePassed = Long.MAX_VALUE;
            System.out.println("********************************************");
            System.out.println("Client name: " + s.name);
            //System.out.println("Active time (ms): " + s.activeNanoTime.longValue()/1e6);
            System.out.println("Messages processed: " + s.messages.longValue());
            System.out.println("Active connection time (ms): " + timePassed/1e6);

            if (s.connection != null) {
                System.out.println("===Connection statistics===");

                long inMessages = s.connection.getStatistics().getInMsgs();
                long inBytes = s.connection.getStatistics().getInBytes();
                double inMessagesSec = inMessages * 1e9 / timePassed;
                double inBytesSec = inBytes * 1e9 / timePassed;
                System.out.printf("In: %18s %18s msg/s %12s/s \n",
                        nf.format(inMessages),
                        nf.format((long)inMessagesSec),
                        Utils.humanBytes(inBytesSec));

                long outMessages = s.connection.getStatistics().getOutMsgs();
                long outBytes = s.connection.getStatistics().getOutBytes();
                double outMessagesSec = outMessages * 1e9 / timePassed;
                double outBytesSec = outBytes * 1e9 / timePassed;
                System.out.printf("Out: %18s %18s msg/s %12s/s \n",
                        nf.format(outMessages),
                        nf.format((long)outMessagesSec),
                        Utils.humanBytes(outBytesSec));

                System.out.printf("Dropped:%d Reconnects:%d Buffer size:%d Connection timeout:%d Max outgoing:%d\n",
                        s.connection.getStatistics().getDroppedCount(),
                        s.connection.getStatistics().getReconnects(),
                        s.connection.getOptions().getBufferSize(),
                        s.connection.getOptions().getConnectionTimeout().getSeconds(),
                        s.connection.getOptions().getMaxMessagesInOutgoingQueue());

            }
            if (s.dispatcher != null){
                System.out.println("===Dispatcher statistics===");
                System.out.printf("Messages delivered/dropped: %d/%d \n",
                        s.dispatcher.getDeliveredCount(),
                        s.dispatcher.getDroppedCount());
                System.out.printf("Pending: %d/(%s) Limit: %d/(%s) \n",
                        s.dispatcher.getPendingMessageCount(),
                        s.dispatcher.getPendingByteCount(),
                        s.dispatcher.getPendingMessageLimit(),
                        s.dispatcher.getPendingByteLimit());
                System.out.printf("Latency(microseconds): min:%.3f max:%.3f avg%.3f p50:%.3f p99:%.3f p99.9:%.3f\n",
                         (s.minLatency / 1e3),
                         (s.maxLatency / 1e3),
                         (s.lCount > 0) ? (s.lSum/s.lCount / 1e3) : 0,
                         s.histogram.getValueAtPercentile(50)/1e3,
                         s.histogram.getValueAtPercentile(99)/1e3,
                         s.histogram.getValueAtPercentile(99.9)/1e3);

                //s.histogram.outputPercentileDistribution(System.out, 2, 1e3, false);

            }
            s.updated = false;
        }
    }

    public void shortPrint()
    {
       NatsStatus sender = clients.stream().filter(c -> c.dispatcher == null).findFirst().get();
       NatsStatus receiver = clients.stream().filter(c -> c.dispatcher != null).findFirst().get();

        long sendingTime = sender.activeNanoTime.longValue();
        long receivingTime = receiver.activeNanoTime.longValue();
        long totalTime = receiver.batchFinishedNanoTime - sender.batchStartedNanoTime;
        long outMessages = sender.connection.getStatistics().getOutMsgs();
        long outBytes = sender.connection.getStatistics().getOutBytes();
        long inMessages = receiver.connection.getStatistics().getInMsgs();
        long inBytes = receiver.connection.getStatistics().getInBytes();
        double sendSpeedBytes = 1e9 * outBytes / sendingTime;
        double receiveSpeedBytes = 1e9 * inBytes / receivingTime;
        double totalSpeedBytes = 1e9 * outBytes / totalTime;
        double sendSpeedMsgs = 1e9 * outMessages / sendingTime;
        double receiveSpeedMsgs = 1e9 * inMessages / receivingTime;
        double totalSpeedMsgs = 1e9 * outMessages / totalTime;

        System.out.printf("Messages: out/in: %d/%d, out/in: %s/%s\n",
                outMessages,
                inMessages,
                Utils.humanBytes(outBytes),
                Utils.humanBytes(inBytes));

        System.out.printf("Time(ms):         sending/receiving/total %.3f / %.3f / %.3f\n",
                sendingTime / 1e6,
                receivingTime / 1e6,
                totalTime / 1e6);

        System.out.printf("Throughput(msgs): sending/receiving/total %.0f/s / %.0f/s /%.0f/s \n",
                sendSpeedMsgs,
                receiveSpeedMsgs,
                totalSpeedMsgs);

        System.out.printf("Throughput(size): sending/receiving/total %s/s / %s/s / %s/s \n",
                Utils.humanBytes(sendSpeedBytes),
                Utils.humanBytes(receiveSpeedBytes),
                Utils.humanBytes(totalSpeedBytes));

        if (receiver.histogram.getTotalCount() > 0)
        {
            System.out.printf("Latency(us): p50:%.3f p99:%.3f p99.9:%.3f\n",
                    receiver.histogram.getValueAtPercentile(50)/1e3,
                    receiver.histogram.getValueAtPercentile(99)/1e3,
                    receiver.histogram.getValueAtPercentile(99.9)/1e3);
            receiver.histogram.outputPercentileDistribution(System.out, 1, 1e3, false);
        }

    }

    public void terminate(){
        isTerminated = true;
    }

    @Override
    public void run() {
       do {
           print();
           try {
               Thread.sleep(timeout);
           } catch (InterruptedException ignored) {

           }
       } while (!isTerminated);
    }
}
