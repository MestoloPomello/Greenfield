package cleaning_robot.beans;

import cleaning_robot.StartCleaningRobot;
import shared.beans.CleaningRobot;
import shared.beans.MechanicRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Mechanic {

    private final static Object lock = new Object();
    private static Mechanic instance;
    private static final List<MechanicRequest> requestQueue = new ArrayList<>();
    private boolean needsFix;

    private int /*neededOKs,*/ receivedOKs;

    private Mechanic() {
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

    public boolean isMyTurn() {
        synchronized (lock) {
            System.out.println("[MECHANIC] NeededOKs: " + (StartCleaningRobot.deployedRobots.getNumber()));
            System.out.println("[MECHANIC] ReceivedOKs: " + receivedOKs);
            return receivedOKs == StartCleaningRobot.deployedRobots.getNumber();
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
            this.needsFix = needsFix;
        }
    }

    public void resetReceivedOKs() {
        synchronized (lock) {
            receivedOKs = 0;
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

    public void notifyForMechanicRelease(int solvedRobotId) {
        MechanicRequest toBeRemoved = null;
        synchronized (requestQueue) {
            for (MechanicRequest mr : requestQueue) {
                if (mr != null && mr.getRobotId() == solvedRobotId) {
                    toBeRemoved = mr;
                    break;
                }
            }
            System.out.println("RequestQueue before: " + requestQueue.toString());
            if (toBeRemoved != null) requestQueue.remove(toBeRemoved);
            System.out.println("RequestQueue after: " + requestQueue.toString());
        }

        acknowledgeOK();
    }

    public List<CleaningRobot> getQueuedRobotsList() {
        List<CleaningRobot> deployedRobots = StartCleaningRobot.deployedRobots.getDeployedRobots();

        List<Integer> robotIds = requestQueue.stream()
                .map(MechanicRequest::getRobotId)
                .collect(Collectors.toList());

        return deployedRobots.stream()
                .filter(robot -> robotIds.contains(robot.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder requestsString = new StringBuilder();
        for (MechanicRequest mr : requestQueue) {
            requestsString.append("\n\t\t").append(mr.toString());
        }
        return "> Mechanic data:" +
                "\n\tneedsFix: " + needsFix +
                "\n\trequests queue:" + requestsString;
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
