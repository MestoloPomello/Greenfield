package cleaning_robot.threads;

import cleaning_robot.StartCleaningRobot;
import cleaning_robot.beans.Mechanic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import shared.beans.MechanicRequest;
import shared.constants.Constants;


public class HealthCheckThread extends Thread {
    private volatile boolean running = true;
    private boolean isRepairing;

    private final List<String> receivedOKs;
    public final Object lock = new Object();

    public HealthCheckThread() {
        super();
        isRepairing = false;
        receivedOKs = new ArrayList<>();
    }

    public void increaseReceivedOKs(int robotId) {
        synchronized (receivedOKs) {
            System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] INGRESSO sezione critica HealthCheckThread");
            receivedOKs.add("r" + robotId);
            System.out.println("+++ ReceivedOKs incrementati. Nuovo valore: " + receivedOKs);
            System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] USCITA sezione critica HealthCheckThread");
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void decreaseReceivedOKs (int robotId) {
        synchronized (receivedOKs) {
            System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] INGRESSO sezione critica HealthCheckThread");
            receivedOKs.remove("r" + robotId);
            System.out.println("--- ReceivedOKs decrementati. Nuovo valore: " + receivedOKs);
            System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] USCITA sezione critica HealthCheckThread");
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void ricartAgrawala() {

        //Mechanic.getInstance().resetReceivedOKs();
        synchronized (receivedOKs) {
            receivedOKs.clear();
        }
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
        while (getReceivedOKsNumber() < StartCleaningRobot.deployedRobots.getNumber()) {
            System.out.println("[HealthCheckThread] Awakened and acknowledged OK, waiting for everyone...");
            try {
                synchronized (lock) {
                    System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] WAIT - INGRESSO sezione critica HealthCheckThread");
                    lock.wait();
                    System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "] WAIT - USCITA sezione critica HealthCheckThread");
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

    private int getReceivedOKsNumber() {
        synchronized (receivedOKs) {
            return receivedOKs.size();
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