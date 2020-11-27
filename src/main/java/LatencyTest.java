import io.nats.client.Options;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class LatencyTest {
    static Options options;

    public static void main(String[] args) throws InterruptedException, TimeoutException, IOException {

       String server = Options.DEFAULT_URL;
       if (args.length > 0)
           server = args[0];

       options = new Options.Builder().
                server(server).
                connectionTimeout(Duration.ofSeconds(1)).
                noReconnect().build();


       System.out.println("**** Warming up, fast sending, no latency check");
       benchmark(1_000_000, 0, false, true);
        System.out.println();

       System.out.println("**** Fast test, fast sending, no latency check");
       benchmark(1_000_000, 0, false, true);
        System.out.println();

       System.out.println("**** Latency test, trying to send 1M/s, latency check ");
       benchmark(1_000_000, 1_000_000, true, true);
        System.out.println();


        System.out.println("**** Latency test, trying to send 500K/s, latency check ");
        benchmark(1_000_000, 500_000, true, true);
        System.out.println();

        System.out.println("**** Latency test, trying to send 100K/s, latency check ");
        benchmark(1_000_000, 100_000, true, true);
        System.out.println();

        System.out.println("**** Latency test, trying to send 50K/s, latency check ");
        benchmark(5_00_000, 50_000, true, true);
        System.out.println();

        System.out.println("**** Latency test, trying to send 10K/s, latency check ");
        benchmark(1_00_000, 10_000, true, true);
        System.out.println();

        System.out.println("**** Latency test, trying to send 5K/s, latency check ");
        benchmark(1_00_000, 5_000, true, true);
        System.out.println();
    }

    public static void benchmark(long msgCount, long msgPerSec, boolean calcLatency, boolean printStat) throws IOException, InterruptedException, TimeoutException {
        StatusThread statusThread = new StatusThread();
        WaitingMessageHandler handler;
        NatsStatus status = new NatsStatus();
        if (calcLatency)
            handler = new LatencyMessageHandler(status);
        else
            handler = new CountMessageHandler(status);
        handler.waitForMessages(msgCount);
        statusThread.addClient(status);
        Subscriber subscriber = new Subscriber(status, handler, options);
        AdjustablePublisher publisher = new AdjustablePublisher(statusThread, options);
        try {
            subscriber.setBufferMessages(10000000);
            subscriber.listen();
            if (msgPerSec > 0)
              publisher.publishSlow(msgCount, msgPerSec);
            else
              publisher.publishFast(msgCount);
        }
        finally{
          subscriber.stop();
          publisher.stop();

        }
        if (printStat)
            statusThread.shortPrint();
        // Ask for GC and wait a moment between tests
        System.gc();
        try {
            Thread.sleep(500);
        } catch (Exception exp) {
            // ignore
        }
    }
}
