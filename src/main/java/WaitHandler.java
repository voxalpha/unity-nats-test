public interface WaitHandler {
    void await() throws InterruptedException;
    void waitForMessages(long msgToWait);
}
