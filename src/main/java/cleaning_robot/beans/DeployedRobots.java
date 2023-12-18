package cleaning_robot.beans;

import shared.beans.CleaningRobot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DeployedRobots {

    private List<CleaningRobot> deployedRobots;

    private final static Object lock = new Object();

    public DeployedRobots(List<CleaningRobot> deployedRobots) {
        this.deployedRobots = deployedRobots;
    }

//    public static DeployedRobots getInstance(int robotId){
//        synchronized(lock) {
//            if (instances.get(robotId) == null)
//                instances.put(robotId, new DeployedRobots());
//            return instances.get(robotId);
//        }
//    }

    public List<CleaningRobot> getDeployedRobots() {
        synchronized (lock) {
            return deployedRobots;
        }
    }

    public int getNumber() {
        synchronized (lock) {
            return deployedRobots.size();
        }
    }

    public int getRobotIndex(CleaningRobot robot) {
        synchronized (lock) {
            return deployedRobots.indexOf(robot);
        }
    }

    public CleaningRobot getRobotByIndex(int index) {
        synchronized (lock) {
            return deployedRobots.get(index);
        }
    }

    public void setDeployedRobots(List<CleaningRobot> deployedRobots) {
        synchronized (lock) {
            this.deployedRobots = deployedRobots;
        }
    }

    public boolean insertRobot(CleaningRobot newRobot){
        try {
            synchronized (lock) {
                deployedRobots.add(newRobot);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteRobot(int id) {
        try {
            synchronized (lock) {
                deployedRobots.removeIf(cr -> cr != null && cr.getId() == id);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public CleaningRobot getSelfReference(int id) {
        for (CleaningRobot cr : deployedRobots) {
            if (cr != null && cr.getId() == id) {
                return cr;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("[LOG] Deployed robots: ");
        for (CleaningRobot cr : deployedRobots) {
            str.append("\n\t").append(cr.toString());
        }
        return str.toString();
    }

}
