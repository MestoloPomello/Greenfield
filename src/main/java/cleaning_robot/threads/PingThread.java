package cleaning_robot.threads;

import cleaning_robot.StartCleaningRobot;
import shared.beans.CleaningRobot;
import shared.constants.Constants;
import static cleaning_robot.StartCleaningRobot.selfReference;
import static cleaning_robot.StartCleaningRobot.deployedRobots;


public class PingThread extends Thread {
    private volatile boolean running = true;

    public PingThread() {
        super();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Crash-check the next robot, but only if there is at least another one
            if (deployedRobots.getNumber() > 1) {
                CleaningRobot nextRobot = findNextRobot();
                System.out.println("[CHECK] Checking if robot " + nextRobot.getId() + " is alive...");
                StartCleaningRobot.sendMessageToOtherRobot(nextRobot, Constants.PING);
            }
        }
    }

    public void stopThread() {
        running = false;
        interrupt();
    }

    public CleaningRobot findNextRobot() {
        int currentIndex = deployedRobots.getRobotIndex(selfReference);
        if (currentIndex == deployedRobots.getNumber() - 1) {
            // If it's the last, return the first robot's port
            return deployedRobots.getRobotByIndex(0);
        } else {
            // Else return the next robot's port
            return deployedRobots.getRobotByIndex(currentIndex + 1);
        }
    }
}