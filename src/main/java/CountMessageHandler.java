import io.nats.client.Message;

public class CountMessageHandler extends WaitingMessageHandler {

    public CountMessageHandler(NatsStatus status){
        super(status);
    }

    @Override
    public void onMessage(Message msg) {
        long start = System.nanoTime();
        status.messages.incrementAndGet();
        long end = System.nanoTime();
        status.activeNanoTime.addAndGet(end - start);
        status.updated = true;
        if (msgToWait <= status.messages.longValue())
        {
            status.batchFinishedNanoTime = System.nanoTime();
            canStop.countDown();
        }

    }
}
