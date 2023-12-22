package cleaning_robot.threads;

import cleaning_robot.StartCleaningRobot;
import shared.constants.Constants;
import simulators.PM10Simulator;
import static cleaning_robot.StartCleaningRobot.selfReference;

import java.util.Scanner;

public class InputThread extends Thread {
    private volatile boolean running = true;
    private final PM10Simulator simulator;

    public InputThread(PM10Simulator simulator) {
        this.simulator = simulator;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (running) {
                String command = scanner.nextLine();

                switch(command) {
                    case Constants.FIX:
                        StartCleaningRobot.healthCheckThread.forceReparation();
                        break;
                    case Constants.QUIT:
                        // finire operazioni meccanico

                        simulator.stopMeGently();
                        StartCleaningRobot.sensorThread.stopThread();

                        // Notify other robots
                        StartCleaningRobot.broadcastMessage(Constants.QUIT);

                        // Notify the server
                        StartCleaningRobot.deleteRequest(StartCleaningRobot.serverAddress + "/robot/" + selfReference.getId());

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
