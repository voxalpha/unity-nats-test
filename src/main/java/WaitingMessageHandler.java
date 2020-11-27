import io.nats.client.Message;
import io.nats.client.MessageHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class WaitingMessageHandler implements MessageHandler, WaitHandler {

    protected NatsStatus status;
    protected CountDownLatch canStop = new CountDownLatch(1);
    protected long msgToWait = 0;

    public WaitingMessageHandler(NatsStatus status){
        this.status = status;
    }


    @Override
    public void await() throws InterruptedException {
        canStop.await(30, TimeUnit.SECONDS);
    }

    @Override
    public void waitForMessages(long msgToWait) {
        this.msgToWait = msgToWait;
    }

}
