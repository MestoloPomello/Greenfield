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
    private MechanicRequest myRequest;

    private Mechanic() {
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

    public void addRobotToMechanicRequests(MechanicRequest newRequest) {
        // Returns the index of the item after being sorted for timestamp
        synchronized (requestQueue) {
            requestQueue.add(newRequest);
            requestQueue.sort(Comparator.comparing(MechanicRequest::getTimestamp));
        }
    }

    public void setMyRequest(MechanicRequest myRequest) {
        this.myRequest = myRequest;
    }

    public MechanicRequest getMyRequest() {
        return myRequest;
    }

    public boolean isFirst(long timestamp) {
        System.out.println("[isFirst] Timestamp arrivato: " + timestamp + ", timestamp mio: " + myRequest.getTimestamp());
        return timestamp < myRequest.getTimestamp();
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
