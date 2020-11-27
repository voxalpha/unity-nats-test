import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Sender {
    public static void main(String[] args) throws InterruptedException, TimeoutException, IOException {
        StatusThread statusThread = new StatusThread();
        Thread daemonThread = new Thread(statusThread);
        daemonThread.setDaemon(true);
        daemonThread.start();

        Publisher publisher = new Publisher(statusThread);
        System.out.println("Connection established");

        boolean terminate = false;
        Scanner sc = new Scanner(System.in);
        while (!terminate)
        {
            System.out.println("Type number of messages to publish or 'q' to quit:");
            String input = sc.nextLine();
            if ("q".equals(input)){
                terminate = true;
            } else
            {
                try {
                    long n = Long.parseLong(input);
                    publisher.publish(n);
                    Thread.sleep(100);
                    statusThread.print();
                }
                catch (NumberFormatException e){
                    System.out.println("Type valid number");
                }
            }
        }
        publisher.stop();
    }
}
