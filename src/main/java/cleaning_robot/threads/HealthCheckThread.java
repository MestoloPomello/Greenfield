package cleaning_robot.threads;

import cleaning_robot.StartCleaningRobot;
import cleaning_robot.beans.DeployedRobots;
import shared.beans.CleaningRobot;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.net.Socket;
//import cleaning_robot.proto.ResearcherOuterClass.Researcher;
import shared.constants.Constants;


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
        // Ask every robot in order to access the mechanic
//        RobotMessage out = RobotMessage.newBuilder()
//                .setSenderId(myId)
//                .setTimestamp(System.currentTimeMillis())
//                .build();
//
//        for (CleaningRobot otherRobot : deployedRobots) {
//            try (Socket s = new Socket(otherRobot.getAddress(), otherRobot.getPort())) {
//                out.writeTo(s.getOutputStream());
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            } finally {
//                s.close();
//            }
//        }

    }

    @Override
    public void run() {
//        Random random = new Random();

        while (running) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Crash-check the next robot, but only if there is at least another one
            // DA SISTEMARE - in caso di crash sembra che chiami il server più volte
            if (deployedRobots.getNumber() > 1) {
                CleaningRobot nextRobot = findNextRobot();
                System.out.println("[CHECK] Checking if robot " + nextRobot.getId() + " is alive...");
                StartCleaningRobot.sendMessageToOtherRobot(nextRobot, Constants.PING);
            }

//            if (!needsFix) {
//                // If the robot is normally working
//                needsFix = random.nextDouble() < 0.1;
//            }

//            if (needsFix) {
//                /*hasToken = false;
//                passToken();*/
//                ricartAgrawala();
//            } else {
//                // Normal waiting time
//            }
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

    public CleaningRobot findNextRobot() {
        int currentIndex = deployedRobots.getRobotIndex(parentRobot);
        if (currentIndex == deployedRobots.getNumber() - 1) {
            // If it's the last, return the first robot's port
            return deployedRobots.getRobotByIndex(0);
        } else {
            // Else return the next robot's port
            return deployedRobots.getRobotByIndex(currentIndex + 1);
        }
    }
}