import io.nats.client.MessageHandler;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Test {
    public static void main(String[] args) throws InterruptedException, TimeoutException, IOException {

        /*StatusThread statusThread = new StatusThread();
        Thread daemonThread = new Thread(statusThread);
        daemonThread.setDaemon(true);
        daemonThread.start();
        Subscriber subscriber = new Subscriber(statusThread);
        subscriber.setBufferBytes(1024*1024*1024*0);
        subscriber.setBufferMessages(10000000);
        subscriber.listen();
        Publisher publisher = new Publisher(statusThread);
        publisher.publish(10000000);
        Thread.sleep(1000);
        subscriber.stop();
        publisher.stop();
        statusThread.print();*/
        //statusThread.terminate();
        /*System.out.println("Handling stat: ");
        System.out.println("Total Receiving time:" +
                (CountMessageHandler.nanoMessageLast - CountMessageHandler.nanoMessageFirst)/1000000);
        System.out.println("Clean Receiving time:" +
                (CountMessageHandler.nano.longValue()/1000000));*/

    }
}
