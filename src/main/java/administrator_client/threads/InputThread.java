package administrator_client.threads;

import java.util.Scanner;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.WebResource;
import shared.beans.CleaningRobot;
import shared.beans.RobotListResponse;

import java.util.List;

public class InputThread extends Thread {

    private volatile boolean running = true;
    private final Client client;
    private final String serverAddress;

    public InputThread(Client client, String serverAddress) {
        this.client = client;
        this.serverAddress = serverAddress;
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
                    RobotListResponse deployedRobotsObj = getDeployedRobots(
                            client,
                            serverAddress + "/client/list_robots"
                    );
                    assert deployedRobotsObj != null;
                    List<CleaningRobot> deployedRobots = deployedRobotsObj.getDeployedRobots();
                    if (deployedRobots.isEmpty()) {
                        System.out.println("> LIST: there are no robots deployed at the moment.");
                    } else {
                        System.out.println("> LIST: ");
                        for (CleaningRobot robot : deployedRobots) {
                            System.out.println("\n\tID: " + robot.getId() +
                                    "\n\tDistrict: " + robot.getDistrictFromPos() +
                                    "\n\tPosition: (" + robot.getPosX() + "," + robot.getPosY() + ")" +
                                    "\n\tPort: " + robot.getPort() + "\n");
                        }
                    }
                    break;
                case "avg_robot":
                    System.out.print("> AVG_ROBOT: choose the robot ID: ");
                    String robotId = scanner.nextLine();

                    System.out.print("> AVG_ROBOT: choose the number of measurements: ");
                    String n = scanner.nextLine();

                    double measurementsAvgRobot = getAvgs(
                            client,
                            serverAddress + "/client/avg_robot/" + robotId + "/" + n
                    );

                    if (measurementsAvgRobot == -1) {
                        System.out.println("> AVG_ROBOT: this robot hasn't uploaded any measurement.");
                        break;
                    }

                    System.out.println("> AVG_ROBOT: average of last " + n + " measurements: " + measurementsAvgRobot);
                    break;
                case "avg_time":
                    System.out.print("> AVG_TIME: enter the first timestamp: ");
                    String t1 = scanner.nextLine();
                    System.out.print("> AVG_TIME: enter the second timestamp: ");
                    String t2 = scanner.nextLine();

                    double measurementsAvgTime = getAvgs(
                            client,
                            serverAddress + "/client/avg_time/" + t1 + "/" + t2
                    );

                    if (measurementsAvgTime == -1) {
                        System.out.println("> AVG_TIME: this robot hasn't uploaded any measurement.");
                        break;
                    }

                    System.out.println("> AVG_TIME: measurements between " + t1 + " and " + t2 + ": " + measurementsAvgTime);
                    break;
                default:
                    System.err.println("[ERROR] Unrecognised command.");
            }
        }
    }

    public void stopThread() {
        running = false;
        interrupt();
    }

    public static RobotListResponse getDeployedRobots(Client client, String url){
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").get(RobotListResponse.class);
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
            return null;
        }
    }

    public static double getAvgs(Client client, String url) {
        WebResource webResource = client.resource(url);
        try {
            String receivedString = webResource.type("application/json").get(String.class);
            if (receivedString.equals("[]")) return -1;
            else return Double.parseDouble(receivedString);
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
            return -1;
        }
    }
}
