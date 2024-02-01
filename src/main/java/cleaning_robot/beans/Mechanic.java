package cleaning_robot.beans;

import cleaning_robot.StartCleaningRobot;
import shared.beans.CleaningRobot;
import shared.beans.MechanicRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Mechanic {

    private final static Object receivedOKsLock = new Object();
    private final static Object lock = new Object();
    private static Mechanic instance;
    private static final List<MechanicRequest> requestQueue = new ArrayList<>();
    private boolean needsFix;
    private MechanicRequest myRequest;

    private int /*neededOKs,*/ receivedOKs;

    private Mechanic() {
        receivedOKs = 0;
        needsFix = false;
        myRequest = null;
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
            return receivedOKs >= StartCleaningRobot.deployedRobots.getNumber();
        }
    }

    public void addRobotToMechanicRequests(MechanicRequest newRequest) {
        // Returns the index of the item after being sorted for timestamp
        synchronized (requestQueue) {
            requestQueue.add(newRequest);
            requestQueue.sort(Comparator.comparing(MechanicRequest::getTimestamp));
            //return requestQueue.indexOf(newRequest);
        }
    }

    public void setMyRequest(MechanicRequest myRequest) {
        this.myRequest = myRequest;
    }

    public MechanicRequest getMyRequest() {
        return myRequest;
    }

    public boolean isFirst(long timestamp) {
        System.out.println("Timestamp arrivato: " + timestamp + ", timestamp mio: " + myRequest.getTimestamp());
        return timestamp < myRequest.getTimestamp();
//        synchronized (requestQueue) {
//            requestQueue.sort(Comparator.comparing(MechanicRequest::getTimestamp));
//            System.out.println("isFirst ordered requestQueue: " + requestQueue);
//            if (requestQueue.isEmpty()) return true;
//            return timestamp < requestQueue.get(0).getTimestamp();
//        }
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

//    public void resetReceivedOKs() {
//        synchronized (lock) {
//            receivedOKs = 0;
//        }
//    }
//
//    public void increaseReceivedOKs() {
//        synchronized (lock) {
//            receivedOKs++;
//            System.out.println("+++ ReceivedOKs incrementati. Nuovo valore: " + receivedOKs);
//        }
//    }
//
//    public void decreaseReceivedOKs () {
//        synchronized (lock) {
//            receivedOKs--;
//            System.out.println("--- ReceivedOKs decrementati. Nuovo valore: " + receivedOKs);
//        }
//    }

    public void acknowledgeOK() {
//        synchronized (receivedOKsLock) {
//            receivedOKs++;
//            System.out.println("+++ ReceivedOKs incrementati. Nuovo valore: " + receivedOKs);
//        }
        synchronized (StartCleaningRobot.healthCheckThread.lock) {
            StartCleaningRobot.healthCheckThread.lock.notifyAll();
//            System.out.println("Thread Svegliato!");
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

    public void resetRequestQueue() {
        synchronized (requestQueue) {
            requestQueue.clear();
        }
    }

    @Override
    public String toString() {
//        StringBuilder requestsString = new StringBuilder();
//        for (MechanicRequest mr : requestQueue) {
//            requestsString.append("\n\t\t").append(mr.toString());
//        }
//        return "> Mechanic data:" +
//                "\n\tneedsFix: " + needsFix +
//                "\n\trequests queue:" + requestsString;
        return requestQueue.toString();
    }

}
