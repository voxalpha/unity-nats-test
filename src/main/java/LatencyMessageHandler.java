import io.nats.client.Message;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class LatencyMessageHandler extends WaitingMessageHandler{

    public LatencyMessageHandler(NatsStatus status) {
        super(status);
    }


    @Override
    public void onMessage(Message msg) {
        long start = System.nanoTime();

        String str = new String(msg.getData(), StandardCharsets.UTF_8);

        if ("start".equals(str)) {
           status.inProgress = true;
           status.batchStartedNanoTime = System.nanoTime();
           canStop = new CountDownLatch(1);
        }
        else if ("stop".equals(str)) {
            status.inProgress= false;
            status.batchFinishedNanoTime = System.nanoTime();
            canStop.countDown();
        }
        else if (str.startsWith("t")) {
            String st = str.substring(1, str.indexOf("n"));
            long sendTime = Long.parseLong(st);
            long latency = start - sendTime;
            //status.lCount++;
            //status.lSum += latency;
            status.histogram.recordValue(latency);
            //if (latency > status.maxLatency) status.maxLatency = latency;
            //if (latency < status.minLatency) status.minLatency = latency;
        }
        status.messages.incrementAndGet();
        long end = System.nanoTime();
        status.activeNanoTime.addAndGet(end - start);
        status.updated = true;
    }
}
