import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class Publisher {

    private Connection connection;
    private StatusThread statusThread;
    private NatsStatus status;

    public void setCommandRepeatCount(int commandRepeatCount) {
        this.commandRepeatCount = commandRepeatCount;
    }

    private int commandRepeatCount = 1;

    public Publisher(StatusThread statusThread) throws IOException, InterruptedException {
        this.statusThread = statusThread;
        //Options.Builder builder = new Options.Builder().
          //      bufferSize(1);
        //Options options = builder.build();
        //connection = Nats.connect(options);
        connection = Nats.connect();
        status = new NatsStatus();
        status.name = "Publisher";
        status.connection = connection;
        statusThread.addClient(status);
    }

   private void sendCommand(String command)
   {
       for (int i = 0; i < commandRepeatCount; i++) {
           connection.publish("subject", command.getBytes());
       }
   }

   void stop() throws TimeoutException, InterruptedException {
       connection.flush(Duration.ofSeconds(15));
       connection.close();
   }

    void publish2(long msgCount) throws IOException, InterruptedException, TimeoutException {
        try (Connection nc = Nats.connect()) {
            byte[] payload = new byte[1024];
            for (int i = 0; i < msgCount; i++) {
                nc.publish("subject", payload);
            }

        }
    }

   void publish(long msgCount) throws IOException, InterruptedException, TimeoutException {
        status.inProgress = true;
        long start = System.nanoTime();
        byte[] payload = new byte[950];
        status.batchStartedNanoTime = start;
        sendCommand("start");
        for (int i = 0; i < msgCount; i++) {
            //if (i%100 == 0) {
                byte[] payloadt = ("t" + System.nanoTime() + "n").getBytes();
                ByteBuffer buff = ByteBuffer.wrap(new byte[1024]);
                buff.put(payloadt);
                buff.put(payload);
                connection.publish("subject", buff.array());
            //}
            //connection.publish("subject", payload);
            status.messages.incrementAndGet();
            status.updated = true;
            //if ()
            //for (int j = 0; j < 300; j++)
              //  System.nanoTime();
            //Thread.sleep(1);
        }
        sendCommand("stop");
        long end = System.nanoTime();
        status.batchFinishedNanoTime = end;
        status.activeNanoTime.addAndGet(end - start);
        status.inProgress = false;
        status.updated = true;
    }

}
