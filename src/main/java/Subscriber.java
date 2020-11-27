import io.nats.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Subscriber {
    private final Connection connection;

    public void setBufferBytes(long bufferBytes) {
        this.bufferBytes = bufferBytes;
    }

    public void setBufferMessages(long bufferMessages) {
        this.bufferMessages = bufferMessages;
    }

    private long bufferBytes = 0;
    private long bufferMessages = 0;
    private final WaitingMessageHandler handler;
    private final NatsStatus status;

    public Subscriber(NatsStatus status, WaitingMessageHandler handler, Options options) throws IOException, InterruptedException {
        this.handler = handler;
        connection = Nats.connect(options);
        this.status = status;
        status.connection = connection;
        status.name = "Subscriber";
        status.connection = connection;
    }

    void listen() {
        Dispatcher dispatcher = connection.createDispatcher(handler);
        if (bufferBytes > 0 || bufferMessages > 0)
          dispatcher.setPendingLimits(bufferMessages, bufferBytes);
        status.dispatcher = dispatcher;
        dispatcher.subscribe("subject", "queue");
    }

    void stop() throws TimeoutException, InterruptedException {
       handler.await();
       connection.flush(null);
       connection.close();
    }

    /*@Override
    public void onMessage(Message msg) throws InterruptedException {
        long start = System.nanoTime();

        String str = new String(msg.getData(), StandardCharsets.UTF_8);

        if ("start".equals(str)) {
            status.inProgress = true;
            stopLatch = new CountDownLatch(1);
            status.batchStartedNanoTime = System.nanoTime();
        }
        else if ("stop".equals(str)) {
            if (stopLatch != null)
                stopLatch.countDown();
            status.inProgress= false;
            status.batchFinishedNanoTime = System.nanoTime();
        }
        else if (str.startsWith("t")) {
            String st = str.substring(1, str.indexOf("n"));
            long sendTime = Long.parseLong(st);
            long latency = start - sendTime;
            status.lCount++;
            status.lSum += latency;
            status.histogram.recordValue(latency);
            if (latency > status.maxLatency) status.maxLatency = latency;
            if (latency < status.minLatency) status.minLatency = latency;
        } else
            status.messages.incrementAndGet();
        if (delay > 0)
            Thread.sleep(delay);
        long end = System.nanoTime();
        status.activeNanoTime.addAndGet(end - start);
        status.updated = true;
    }*/
}
