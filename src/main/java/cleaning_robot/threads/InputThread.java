package cleaning_robot.threads;

import java.io.IOException;
import java.util.Scanner;

public class InputThread extends Thread {
    private volatile boolean running = true;
    private final int robotId;

    public InputThread(int robotId) {
        this.robotId = robotId;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (running) {
            //try {
                System.out.print("> Insert a command for this robot (ID: " + robotId + "): ");
                String command = scanner.nextLine();

                switch(command) {
                    case "fix":

                        break;
                    case "quit":
                        // finire operazioni meccanico
                        // notificare gli altri robot via grpc
                        // richiedere al server di poter lasciare greenfield
                        stopThread();
                        break;
                    default:
                        System.out.println("[ERROR] Unrecognised command.");
                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        }
    }

    public void stopThread() {
        running = false;
        interrupt();
    }
}
