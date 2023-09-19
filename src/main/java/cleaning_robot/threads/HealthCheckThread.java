package cleaning_robot.threads;

import shared.beans.CleaningRobot;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.net.Socket;
//import cleaning_robot.proto.ResearcherOuterClass.Researcher;
import shared.constants.Constants;


public class HealthCheckThread extends Thread {
    private volatile boolean running = true;
    private int myId, myPortNumber;
    private boolean needsFix, hasToken;
    private List<CleaningRobot> deployedRobots;

    public HealthCheckThread(int myId, int myPortNumber, List<CleaningRobot> deployedRobots) {
        super();
        needsFix = false;
        hasToken = false;
        this.myId = myId;
        this.myPortNumber = myPortNumber;
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
        Random random = new Random();

        while (running) {
            if (needsFix) {
                /*hasToken = false;
                passToken();*/
                ricartAgrawala();
            } else {
                // Normal waiting time
                try {
                    TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                needsFix = random.nextDouble() < 0.1;
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