package cleaning_robot;

import cleaning_robot.proto.RobotCommunicationServiceGrpc.*;
import cleaning_robot.proto.RobotMessageOuterClass.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import shared.beans.CleaningRobot;
import shared.constants.Constants;
import shared.exceptions.UnrecognisedMessageException;
import shared.utils.LamportTimestamp;

import java.io.IOException;
import java.util.List;

public class RobotCommunicationServiceImpl extends RobotCommunicationServiceImplBase {

    CleaningRobot parentRobot;
    List<CleaningRobot> deployedRobots;
    io.grpc.Server server;
    LamportTimestamp timestamp;

    public RobotCommunicationServiceImpl() { super(); }

    public RobotCommunicationServiceImpl(CleaningRobot parentRobot, List<CleaningRobot> deployedRobots, LamportTimestamp timestamp) {
        super();
        this.parentRobot = parentRobot;
        this.deployedRobots = deployedRobots;
        this.timestamp = timestamp;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    public StreamObserver<RobotMessage> rcs(StreamObserver<RobotMessage> responseObserver) {

        return new StreamObserver<RobotMessage>() {

            public void onNext(RobotMessage robotMessage) {

                final String msg = robotMessage.getMessage();
                System.out.println("[IN] From ("
                        + robotMessage.getSenderId()
                        + ": "
                        + robotMessage.getSenderPort()
                        + "): "
                        + msg);

                try {
                    switch (msg) {
                        case Constants.HELLO:
                            // Saving robot in local list
                            deployedRobots.add(new CleaningRobot(
                                    robotMessage.getSenderId(),
                                    robotMessage.getSenderPort(),
                                    Constants.SERVER_ADDR,
                                    robotMessage.getStartingPosX(),
                                    robotMessage.getStartingPosY()
                            ));

                            System.out.println("[HELLO] Acknowledged robot with port " +
                                    robotMessage.getSenderPort() + " and ID " + robotMessage.getSenderId());

                            // Compare and increase the local timestamp with the one received from the new robot
                            int newTimestamp = timestamp.compareAndIncreaseTimestamp(robotMessage.getTimestamp());

                            // Ack response
                            responseObserver.onNext(RobotMessage.newBuilder()
                                    .setSenderId(parentRobot.getId())
                                    .setSenderPort(parentRobot.getPort())
                                    .setTimestamp(newTimestamp)
                                    .setStartingPosX(parentRobot.getPosX())
                                    .setStartingPosY(parentRobot.getPosY())
                                    .setMessage(Constants.HELLO)
                                    .build());
                            break;
                        case Constants.QUIT:
                            deployedRobots.removeIf(cr -> cr.getId() == robotMessage.getSenderId());
                            //server.shutdown();

                            System.out.println("[QUIT] Acknowledged that robot with ID "
                                    + robotMessage.getSenderId() + " has quit Greenfield.");
                            break;
                        case Constants.REQ_MECHANIC:
                            break;
                        case Constants.PING:
                            break;
                        default:
                            // Crashed robot message - crash_{id}
                            try {
                                int crashedRobot = Integer.parseInt(robotMessage.getMessage().split("_")[1]);
                                deployedRobots.removeIf(cr -> cr.getId() == crashedRobot);
                                System.out.println("[CRASH] Acknowledged that robot with ID "
                                        + crashedRobot + " has crashed.");
                            } catch (Exception e){
                                throw new UnrecognisedMessageException(msg);
                            }
                    }
                } catch (UnrecognisedMessageException e) {
                    e.printStackTrace();
                }
            }

            public void onError(Throwable throwable) {
            }

            public void onCompleted() {
            }
        };
    }

}
