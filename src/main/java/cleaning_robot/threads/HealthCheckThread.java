package cleaning_robot.threads;

import cleaning_robot.StartCleaningRobot;
import cleaning_robot.beans.Mechanic;
import java.util.Random;

import shared.beans.MechanicRequest;
import shared.constants.Constants;


public class HealthCheckThread extends Thread {
    private volatile boolean running = true;
    private boolean isRepairing;

    public final Object lock = new Object();

    public HealthCheckThread() {
        super();
        isRepairing = false;
    }

    public void ricartAgrawala() {
        Mechanic.getInstance().resetReceivedOKs();
        System.out.println("[FIX] Starting reparation process...");

        // Pause the measurements thread
        StartCleaningRobot.sensorThread.setInReparation();

        // Create my request
        Mechanic.getInstance().setMyRequest(new MechanicRequest(
                StartCleaningRobot.id,
                StartCleaningRobot.timestamp.getTimestamp()
        ));

        // Ask every robot for permission (including itself)
        StartCleaningRobot.broadcastMessage_All(Constants.NEED_MECHANIC, true);

        // If it's not my turn, wait
        while (!Mechanic.getInstance().isMyTurn()) {
            System.out.println("[HealthCheckThread] Not my turn, waiting...");
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        System.out.println("[HealthCheckThread] My turn, repairing...");
        isRepairing = true;

        // Simulate the reparation
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        isRepairing = false;

        // Release the mechanic and notify the other robots
        Mechanic.getInstance().setNeedsFix(false);

        StartCleaningRobot.broadcastMessage(
                Constants.MECHANIC_OK,
                false,
                Mechanic.getInstance().getQueuedRobotsList()
        );

        // Clear the queue
        Mechanic.getInstance().resetRequestQueue();

        // Resume the measurements thread
        synchronized (StartCleaningRobot.sensorThread.lock) {
            StartCleaningRobot.sensorThread.lock.notifyAll();
        }
        StartCleaningRobot.sensorThread.setReparationEnded();

        System.out.println("[HealthCheckThread] Finished reparation");

        // Resume the input thread if it was waiting for this before the quit
        synchronized (StartCleaningRobot.inputThread.lock) {
            StartCleaningRobot.inputThread.lock.notifyAll();
        }
    }

    public void forceReparation() {
        if (Mechanic.getInstance().isNeedsFix()) {
            System.out.println("[HealthCheckThread] There is already an ongoing reparation.");
        } else {
            Mechanic.getInstance().setNeedsFix(true);
            ricartAgrawala();
        }
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

            if (!Mechanic.getInstance().isNeedsFix()) {
                Mechanic.getInstance().setNeedsFix(random.nextDouble() < 0.1);

                if (Mechanic.getInstance().isNeedsFix()) {
                    ricartAgrawala();
                }
            }
        }
    }

    public void stopThread() {
        running = false;
        interrupt();
    }

    public boolean isRepairing() {
        return isRepairing;
    }

    public void setRepairing(boolean repairing) {
        isRepairing = repairing;
    }
}