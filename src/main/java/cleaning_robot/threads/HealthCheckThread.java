package cleaning_robot.threads;

import cleaning_robot.StartCleaningRobot;
import cleaning_robot.beans.Mechanic;
import java.util.Random;
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
        isRepairing = true;
        System.out.println("[FIX] Starting reparation...");

        // Pause the measurements thread
        StartCleaningRobot.sensorThread.setInReparation();

        // Ask every robot for permission (including itself)
        Mechanic.getInstance().setNeededOKs(StartCleaningRobot.deployedRobots.getNumber());
        StartCleaningRobot.broadcastMessage(Constants.NEED_MECHANIC, true);

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

//        // Adds itself to its own requests queue
//        StartCleaningRobot.addRobotToMechanicRequests(new MechanicRequest(selfReference.getId(), timestamp.getTimestamp()));
//
//        // Starts
//        StartCleaningRobot.waitForMechanicTurn(timestamp.getTimestamp());

        // Simulate the reparation
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Release the mechanic and notify the other robots
        StartCleaningRobot.broadcastMessage(Constants.MECHANIC_RELEASE, false);
        Mechanic.getInstance().setNeedsFix(false);

        // Restart the measurements thread
        synchronized (StartCleaningRobot.sensorThread.lock) {
            StartCleaningRobot.sensorThread.lock.notifyAll();
        }
        StartCleaningRobot.sensorThread.setReparationEnded();

        System.out.println("[HealthCheckThread] Finished reparation");

        // Resume the input thread if it was waiting for this before the quit
        isRepairing = false;
        synchronized (StartCleaningRobot.inputThread.lock) {
            StartCleaningRobot.inputThread.lock.notifyAll();
        }
    }

    public void forceReparation() {
        Mechanic.getInstance().setNeedsFix(true);
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

            if (!Mechanic.getInstance().isNeedsFix()) {
                Mechanic.getInstance().setNeedsFix(random.nextDouble() < 0.1);
            }

            if (Mechanic.getInstance().isNeedsFix() && !isRepairing) {
                ricartAgrawala();
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