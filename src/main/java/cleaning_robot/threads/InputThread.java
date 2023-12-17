package cleaning_robot.threads;

import cleaning_robot.StartCleaningRobot;
import com.sun.jersey.api.client.Client;
import shared.beans.CleaningRobot;
import shared.beans.RobotCreationResponse;
import shared.constants.Constants;
import simulators.PM10Simulator;

import java.io.IOException;
import java.util.Scanner;

public class InputThread extends Thread {
    private volatile boolean running = true;
    private final CleaningRobot parentRobot;
    private final Client serverClient;
    private final PM10Simulator simulator;
    private final SensorThread sensorThread;

    public InputThread(CleaningRobot parentRobot, Client serverClient, PM10Simulator simulator, SensorThread sensorThread) {
        this.parentRobot = parentRobot;
        this.serverClient = serverClient;
        this.simulator = simulator;
        this.sensorThread = sensorThread;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (running) {
                String command = scanner.nextLine();

                switch(command) {
                    case "fix":

                        break;
                    case Constants.QUIT:
                        // finire operazioni meccanico

                        simulator.stopMeGently();
                        sensorThread.stopThread();

                        // Notify other robots
                        StartCleaningRobot.broadcastMessage(Constants.QUIT);

                        // Notify the server
                        StartCleaningRobot.deleteRequest(StartCleaningRobot.serverAddress + "/robot/" + parentRobot.getId());

                        System.out.println("[QUIT] Server acknowledged.");

                        stopThread();
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
