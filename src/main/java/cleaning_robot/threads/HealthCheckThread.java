package cleaning_robot.threads;

import cleaning_robot.StartCleaningRobot;
import cleaning_robot.beans.DeployedRobots;
import cleaning_robot.proto.RobotMessageOuterClass.RobotMessage;
import shared.beans.CleaningRobot;

import java.util.List;
import java.util.Random;
import shared.constants.Constants;

import static cleaning_robot.StartCleaningRobot.syncBroadcastMessage;


public class HealthCheckThread extends Thread {
    private volatile boolean running = true;
    private CleaningRobot parentRobot;
    private boolean needsFix, hasToken;
    private DeployedRobots deployedRobots;

    public HealthCheckThread(CleaningRobot parentRobot, DeployedRobots deployedRobots) {
        super();
        needsFix = false;
        hasToken = false;
        this.parentRobot = parentRobot;
        this.deployedRobots = deployedRobots;
    }

    public void ricartAgrawala() {
        // Ask every robot for permission
        List<RobotMessage> responses = StartCleaningRobot.syncBroadcastMessage(Constants.NEED_MECHANIC);

        // Check if all responses are OK
        if (responses.stream().allMatch(response -> response.getMessage().equals(Constants.MECHANIC_OK))) {
            // Reparation
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // After finishing with the mechanic, release the mechanic
            StartCleaningRobot.syncBroadcastMessage(Constants.MECHANIC_RELEASE);
        } else {
            // Handle the case when access is not granted
            // Add request to the queue and wait for mechanic availability
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

            if (!needsFix) {
                // If the robot is normally working
                needsFix = random.nextDouble() < 0.1;
            }

            if (needsFix) {
                /*hasToken = false;
                passToken();*/
                ricartAgrawala();
            } else {
                // Normal waiting time
            }
        }
    }

    /*private void passToken() {
        CleaningRobot nextRobot = null;
        Iterator<CleaningRobot> it = deployedRobots.iterator();
        while (it.hasNext()) {
            CleaningRobot r = it.next();
            if (r.getId() == myId) {
                nextRobot = it.next();
                break;
            }
        }
        // If this is the last robot, start from the first (ring algorithm)
        if (nextRobot == null) nextRobot = deployedRobots.get(0);

        // Pass the token to the next robot
        try (Socket s = new Socket(nextRobot.getAddress(), nextRobot.getPort())){
            // DA SISTEMARE: controllare che il prossimo robot non sia crashato
            String response =
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    public void stopThread() {
        running = false;
        interrupt();
    }
}