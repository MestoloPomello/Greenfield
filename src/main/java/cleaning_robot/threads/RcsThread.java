package cleaning_robot.threads;

import cleaning_robot.RobotCommunicationServiceImpl;
import io.grpc.ServerBuilder;
import static cleaning_robot.StartCleaningRobot.selfReference;
import static cleaning_robot.StartCleaningRobot.deployedRobots;
import static cleaning_robot.StartCleaningRobot.timestamp;

import java.awt.*;
import java.io.IOException;

public class RcsThread extends Thread  {

    public RobotCommunicationServiceImpl service;

    public RcsThread() {
        service = new RobotCommunicationServiceImpl(
                selfReference,
                deployedRobots,
                timestamp
        );
    }

    @Override
    public void run() {
        try {
            io.grpc.Server server = ServerBuilder
                    .forPort(selfReference.getPort())
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
