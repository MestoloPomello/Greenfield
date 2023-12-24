package cleaning_robot.threads;

import cleaning_robot.StartCleaningRobot;
import shared.constants.Constants;
import simulators.PM10Simulator;
import static cleaning_robot.StartCleaningRobot.selfReference;

import java.util.Scanner;

public class InputThread extends Thread {
    private volatile boolean running = true;
    private final PM10Simulator simulator;

    public final Object lock = new Object();

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
                        // If the robot is being repaired, wait
                        if (StartCleaningRobot.healthCheckThread.isRepairing()) {
                            System.out.println("[INPUT] Quit command queued. Waiting for the reparation to end...");
                            synchronized (lock) {
                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    return;
                                }
                            }
                        }

                        simulator.stopMeGently();
                        StartCleaningRobot.sensorThread.stopThread();

                        // Notify other robots
                        StartCleaningRobot.broadcastMessage(Constants.QUIT, false);

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
