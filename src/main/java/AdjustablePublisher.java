import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AdjustablePublisher implements Runnable{

    private Connection connection;
    private StatusThread statusThread;
    private NatsStatus status;

    public void setCommandRepeatCount(int commandRepeatCount) {
        this.commandRepeatCount = commandRepeatCount;
    }

    private int commandRepeatCount = 1;
    private long period;
    private long toPublish;
    //private Lock canTerminate = new ReentrantLock();
    private CountDownLatch latch;

    public AdjustablePublisher(StatusThread statusThread, Options options) throws IOException, InterruptedException {
        this.statusThread = statusThread;
        connection = Nats.connect(options);
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



   void publishFast(long msgCount)
   {
       status.inProgress = true;
       long start = System.nanoTime();
       status.batchStartedNanoTime = start;
       byte[] payload = new byte[1024];
       for (int i = 0; i < msgCount; i++) {
           connection.publish("subject", payload);
       }
       long end = System.nanoTime();
       status.batchFinishedNanoTime = end;
       status.activeNanoTime.addAndGet(end - start);
       status.inProgress = false;
   }


   void publishSlow(long msgCount, long msgPerSec) throws IOException, InterruptedException, TimeoutException {
       status.inProgress = true;
       toPublish = msgCount;
       long period = (long) 1e9 / msgPerSec;

       sendCommand("start");
       latch = new CountDownLatch(1);

       ScheduledExecutorService scheduledExecutorService =
               Executors.newScheduledThreadPool(1);

       scheduledExecutorService.scheduleAtFixedRate(this, 0, period, TimeUnit.NANOSECONDS);

       long start = System.nanoTime();
       status.batchStartedNanoTime = start;
       latch.await();
       sendCommand("stop");
       long end = System.nanoTime();
       scheduledExecutorService.shutdown();
       status.batchFinishedNanoTime = end;
       status.activeNanoTime.addAndGet(end - start);
       status.inProgress = false;
    }

    @Override
    public void run() {
        if (status.messages.longValue() < toPublish){
            byte[] payloadt = ("t" + System.nanoTime() + "n").getBytes();
            ByteBuffer buff = ByteBuffer.wrap(new byte[1024]);
            buff.put(payloadt);
            buff.put(new byte[1024 - payloadt.length]);
            connection.publish("subject", buff.array());
            status.messages.incrementAndGet();
        } else
        {
            latch.countDown();
        }
        status.updated = true;
    }
}
