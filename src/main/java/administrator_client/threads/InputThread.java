package administrator_client.threads;

import java.util.ArrayList;
import java.util.Scanner;
import administrator_server.beans.CleaningRobots;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.GenericType;
import shared.beans.CleaningRobot;
import shared.beans.MeasurementsListResponse;
import shared.beans.RobotListResponse;
import shared.beans.ServerMeasurement;
import simulators.Measurement;

import javax.ws.rs.core.MediaType;
import java.util.List;

public class InputThread extends Thread {

    private volatile boolean running = true;
    private Client client;
    private String serverAddress;

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

                    MeasurementsListResponse measurementsAvgsRobotObj = getAvgs(
                            client,
                            serverAddress + "/client/avg_robot/" + robotId + "/" + n
                    );

                    if (measurementsAvgsRobotObj == null) {
                        System.out.println("> AVG_ROBOT: null response.");
                        break;
                    }

                    System.out.println("MeasurementsListResponse:" + measurementsAvgsRobotObj);

                    // DA SISTEMARE: risposta vuota

                    List<ServerMeasurement> robotMeasurementsList = measurementsAvgsRobotObj.getMeasurementsList();
                    if (robotMeasurementsList.isEmpty()) {
                        System.out.println("> AVG_ROBOT: this robot hasn't uploaded any measurement.");
                        break;
                    } else {
                        System.out.println("> AVG_ROBOT: last " + n + " measurements:");
                        for (ServerMeasurement sm : robotMeasurementsList) {
                            System.out.println("\n\tTimestamp: " + sm.getTimestamp() +
                                    "\n\tValue: " + sm.getValue() +
                                    "\n\tDistrict: " + sm.getDistrict() + "\n");
                        }
                    }
                    break;
                case "avg_time":
                    System.out.print("> AVG_TIME: enter the first timestamp: ");
                    String t1 = scanner.nextLine();
                    System.out.print("> AVG_TIME: enter the second timestamp: ");
                    String t2 = scanner.nextLine();

                    MeasurementsListResponse measurementsAvgsTimeObj = getAvgs(
                            client,
                            serverAddress + "/client/avg_time/" + t1 + "/" + t2
                    );

                    if (measurementsAvgsTimeObj == null) {
                        System.out.println("> AVG_TIME: null response.");
                        break;
                    }

                    System.out.println("MeasurementsListResponse:" + measurementsAvgsTimeObj);

                    List<ServerMeasurement> timeMeasurementsList = measurementsAvgsTimeObj.getMeasurementsList();
                    if (timeMeasurementsList.isEmpty()) {
                        System.out.println("> AVG_TIME: this robot hasn't uploaded any measurement.");
                        break;
                    } else {
                        System.out.println("> AVG_TIME: measurements between " + t1 + " and " + t2 + ":");
                        for (ServerMeasurement sm : timeMeasurementsList) {
                            System.out.println("\n\tTimestamp: " + sm.getTimestamp() +
                                    "\n\tValue: " + sm.getValue() +
                                    "\n\tDistrict: " + sm.getDistrict() + "\n");
                        }
                    }
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

    public static MeasurementsListResponse getAvgs(Client client, String url) {
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").get(MeasurementsListResponse.class);
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
            return null;
        }
    }
}
