package cleaning_robot.beans;

import cleaning_robot.StartCleaningRobot;
import shared.beans.MechanicRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Mechanic {

    private final static Object lock = new Object();
    private static Mechanic instance;
    private static final List<MechanicRequest> requestQueue = new ArrayList<>();
    private static boolean needsFix;

    private int neededOKs, receivedOKs;

    private Mechanic() {
        neededOKs = 0;
        receivedOKs = 0;
        needsFix = false;
    }

    // Singleton
    public static Mechanic getInstance() {
        synchronized (lock) {
            if (instance == null)
                instance = new Mechanic();
            return instance;
        }
    }

    public void acknowledgeOK() {
        synchronized (lock) {
            receivedOKs++;
        }
        synchronized (StartCleaningRobot.healthCheckThread.lock) {
            StartCleaningRobot.healthCheckThread.lock.notifyAll();
        }
    }

    public boolean isMyTurn() {
        synchronized (lock) {
            System.out.println("[MECHANIC] NeededOKs: " + (neededOKs - 1));
            System.out.println("[MECHANIC] ReceivedOKs: " + receivedOKs);
            return receivedOKs == neededOKs;
        }
    }

    public int addRobotToMechanicRequests(MechanicRequest newRequest) {
        // Returns the index of the item after being sorted for timestamp
        synchronized (requestQueue) {
            requestQueue.add(newRequest);
            requestQueue.sort(Comparator.comparing(MechanicRequest::getTimestamp));
            return requestQueue.indexOf(newRequest);
        }
    }

    public boolean isNeedsFix() {
        synchronized (lock) {
            return needsFix;
        }
    }

    public void setNeedsFix(boolean needsFix) {
        synchronized (lock) {
            Mechanic.needsFix = needsFix;
            neededOKs = 0;
            receivedOKs = 0;
        }
    }

    public void setNeededOKs(int neededOKs) {
        this.neededOKs = neededOKs;
    }

    public void notifyForMechanicRelease(int solvedRobotId) {
        synchronized (requestQueue) {
            MechanicRequest toBeRemoved = null;
            for (MechanicRequest mr : requestQueue) {
                if (mr != null && mr.getRobotId() == solvedRobotId) {
                    toBeRemoved = mr;
                    break;
                }
            }
            requestQueue.remove(toBeRemoved);
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }


//    public static void waitForMechanicTurn(long reqTimestamp) {
//        synchronized (lock) {
//            addRobotToMechanicRequests(new MechanicRequest(StartCleaningRobot.id, reqTimestamp));
//
//            // If there are one or more robots that came first, wait for a notify
//            while (!requestQueue.isEmpty() && requestQueue.get(0).getRobotId() == StartCleaningRobot.id) {
//                try {
//                    lock.wait();
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//
//            // Remove the first request (this) from the list and notify the other robots
//            requestQueue.remove(0);
//
//            // Tell the other robots that this one has finished
//            StartCleaningRobot.broadcastMessage(Constants.MECHANIC_RELEASE);
//        }
//
//    }
//
//    public static List<MechanicRequest> getMechanicRequests() {
//        synchronized (requestQueue) {
//            return requestQueue;
//        }
//    }

}
