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
                CleaningRobot nextRobot = deployedRobots.findNextRobot(selfReference);
                //System.out.print("[CHECK] Checking if robot " + nextRobot.getId() + " is alive...");
                StartCleaningRobot.sendMessageToOtherRobot(nextRobot, Constants.PING);
            }
        }
    }

    public void stopThread() {
        running = false;
        interrupt();
    }
}