package cleaning_robot.threads;

import cleaning_robot.StartCleaningRobot;
import shared.beans.CleaningRobot;

import java.util.List;
import java.util.Random;

import shared.beans.MechanicRequest;
import shared.constants.Constants;

import static cleaning_robot.StartCleaningRobot.timestamp;
import static cleaning_robot.StartCleaningRobot.selfReference;


public class HealthCheckThread extends Thread {
    private volatile boolean running = true;
    private boolean needsFix;

    public HealthCheckThread() {
        super();
        needsFix = false;
    }

    public void ricartAgrawala() {
        System.out.println("[FIX] Starting reparation...");

        // Pause the measurements thread
        StartCleaningRobot.sensorThread.setInReparation();

        // Ask every robot for permission
        StartCleaningRobot.broadcastMessage(Constants.NEED_MECHANIC);

        // Adds itself to its own requests queue
        StartCleaningRobot.addRobotToMechanicRequests(new MechanicRequest(selfReference.getId(), timestamp.getTimestamp()));

        // Starts
        StartCleaningRobot.waitForMechanicTurn(timestamp.getTimestamp());

        // Simulate the reparation
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Release the mechanic and notify the other robots
        StartCleaningRobot.syncBroadcastMessage(Constants.MECHANIC_RELEASE);
        needsFix = false;

        // Restart the measurements thread
        StartCleaningRobot.class.notifyAll();
        StartCleaningRobot.sensorThread.setReparationEnded();

        System.out.println("[FIX] Reparation successfully completed.");
    }

    public void forceReparation() {
        needsFix = true;
        ricartAgrawala();
    }

    @Override
    public void run() {
        Random random = new Random();

        while (running) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (!needsFix) {
                needsFix = random.nextDouble() < 0.1;
            }

            if (needsFix) {
                ricartAgrawala();
            }
        }
    }

    public void stopThread() {
        running = false;
        interrupt();
    }
}