package cleaning_robot.threads;

import cleaning_robot.RobotCommunicationServiceImpl;
import io.grpc.ServerBuilder;
import static cleaning_robot.StartCleaningRobot.selfReference;
import static cleaning_robot.StartCleaningRobot.deployedRobots;
import static cleaning_robot.StartCleaningRobot.timestamp;

import java.io.IOException;

public class RcsThread extends Thread  {

    public RcsThread() { }

    @Override
    public void run() {
        try {
            RobotCommunicationServiceImpl service = new RobotCommunicationServiceImpl(
                    selfReference,
                    deployedRobots,
                    timestamp
            );

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
