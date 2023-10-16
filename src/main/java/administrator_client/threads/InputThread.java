package administrator_client.threads;

import java.util.Scanner;
import administrator_server.beans.CleaningRobots;

public class InputThread extends Thread {
    private volatile boolean running = true;

    public InputThread() {

    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (running) {
            System.out.println("> Select an operation: " +
                    "\n\t- \"list\": list the current deployed robots" +
                    "\n\t- \"avg_robot\": average of last n air pollution levels sent to the server by a given robot" +
                    "\n\t- \"avg_time\": average of the air pollution levels sent by all the robots occurred between timestamps t1 and t2");

            String command = scanner.nextLine();
            switch(command) {
                case "list":
                    break;
                case "avg_robot":
                    break;
                case "avg_timestamp":
                    break;
                default:
                    System.out.println("[ERROR] Unrecognised command.");
            }
        }
    }

    public void stopThread() {
        running = false;
        interrupt();
    }
}
