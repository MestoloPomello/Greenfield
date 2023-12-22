package cleaning_robot.threads;

import cleaning_robot.RobotCommunicationServiceImpl;
import cleaning_robot.beans.DeployedRobots;
import io.grpc.ServerBuilder;
import shared.beans.CleaningRobot;
import shared.utils.LamportTimestamp;

import java.io.IOException;

public class RcsThread extends Thread  {

    private final CleaningRobot parentRobot;
    private final DeployedRobots deployedRobots;
    private final LamportTimestamp timestamp;

    public RcsThread(CleaningRobot parentRobot, DeployedRobots deployedRobots, LamportTimestamp timestamp) {
        this.parentRobot = parentRobot;
        this.deployedRobots = deployedRobots;
        this.timestamp = timestamp;
    }

    @Override
    public void run() {
        try {
            RobotCommunicationServiceImpl service = new RobotCommunicationServiceImpl(
                    parentRobot,
                    deployedRobots,
                    timestamp
            );

            io.grpc.Server server = ServerBuilder
                    .forPort(parentRobot.getPort())
                    .addService(service)
                    .build();
            service.setServer(server);
            server.start();
            System.out.println("[RCS] Opened listening service for other robots.");
            server.awaitTermination();
        } catch (IOException e) {
            System.err.println("[RCS] IOException: " + e.getMessage());
            interrupt();
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("[RCS] InterruptException: " + e.getMessage());
            interrupt();
            e.printStackTrace();
        }
    }

    public void stopThread() {
        interrupt();
    }

}
