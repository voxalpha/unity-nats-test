import io.nats.client.Options;

import java.io.IOException;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Receiver {
    public static void main(String[] args) throws InterruptedException, TimeoutException, IOException {
        StatusThread statusThread = new StatusThread();
        Thread daemonThread = new Thread(statusThread);
        daemonThread.setDaemon(true);
        daemonThread.start();

        NatsStatus status = new NatsStatus();
        WaitingMessageHandler handler = new LatencyMessageHandler(status);
        statusThread.addClient(status);
        Subscriber subscriber = new Subscriber(status, handler, new Options.Builder().build());
        subscriber.setBufferBytes(0);
        subscriber.setBufferMessages(1000000);
        subscriber.listen();
        System.out.println("Connection established");

        boolean terminate = false;
        Scanner sc = new Scanner(System.in);
        while (!terminate)
        {
            System.out.println("Type delay(ms) to process message or 'q' to quit:");
            String input = sc.nextLine();
            if ("q".equals(input)){
                terminate = true;
            } else
            {
                try {
                    int i = Integer.parseInt(input);
                    //subscriber.setDelay(i);
                }
                catch (NumberFormatException e){
                    System.out.println("Type valid number");
                }
            }
        }
        subscriber.stop();
    }
}
