package cleaning_robot.beans;

import shared.beans.CleaningRobot;

import java.util.List;

import static cleaning_robot.StartCleaningRobot.deployedRobots;
import static cleaning_robot.StartCleaningRobot.selfReference;


public class DeployedRobots {

    private List<CleaningRobot> deployedRobots;

    private final static Object lock = new Object();

    public DeployedRobots(List<CleaningRobot> deployedRobots) {
        this.deployedRobots = deployedRobots;
    }

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

    public void insertRobot(CleaningRobot newRobot) {
        synchronized (lock) {
            deployedRobots.add(newRobot);
        }
    }

    public void deleteRobot(int id) {
        synchronized (lock) {
            deployedRobots.removeIf(cr -> cr != null && cr.getId() == id);
        }
    }

    public CleaningRobot getSelfReference(int id) {
        synchronized (lock) {
            for (CleaningRobot cr : deployedRobots) {
                if (cr != null && cr.getId() == id) {
                    return cr;
                }
            }
        }
        return null;
    }

    public CleaningRobot findNextRobot(CleaningRobot robot) {
        int currentIndex = getRobotIndex(robot);
        if (currentIndex == getNumber() - 1) {
            // If it's the last, return the first robot's port
            return getRobotByIndex(0);
        } else {
            // Else return the next robot's port
            return getRobotByIndex(currentIndex + 1);
        }
    }

    public void changeRobotDistrict(int movedRobotId, int newPosX, int newPosY) {
        synchronized (lock) {
            for (CleaningRobot cr : deployedRobots) {
                if (cr != null && cr.getId() == movedRobotId) {
                    cr.setPosX(newPosX);
                    cr.setPosY(newPosY);
                    break;
                }
            }
        }
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
