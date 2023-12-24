package cleaning_robot;

import cleaning_robot.beans.DeployedRobots;
import cleaning_robot.beans.Mechanic;
import cleaning_robot.proto.RobotCommunicationServiceGrpc.*;
import cleaning_robot.proto.RobotMessageOuterClass.*;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import shared.beans.CleaningRobot;
import shared.beans.MechanicRequest;
import shared.constants.Constants;
import shared.exceptions.UnrecognisedMessageException;
import shared.utils.LamportTimestamp;

import java.awt.*;

public class RobotCommunicationServiceImpl extends RobotCommunicationServiceImplBase {

    CleaningRobot parentRobot;
    DeployedRobots deployedRobots;
    io.grpc.Server server;
    LamportTimestamp timestamp;

    public RobotCommunicationServiceImpl() {
        super();
    }

    public RobotCommunicationServiceImpl(CleaningRobot parentRobot, DeployedRobots deployedRobots, LamportTimestamp timestamp) {
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

    public RobotMessage buildMessage(String msg, int newTimestamp) {
        return RobotMessage.newBuilder()
                .setSenderId(parentRobot.getId())
                .setSenderPort(parentRobot.getPort())
                .setTimestamp(newTimestamp)
                .setStartingPosX(parentRobot.getPosX())
                .setStartingPosY(parentRobot.getPosY())
                .setMessage(msg)
                .build();
    }

    public void handleRobotMessage(
            RobotMessage robotMessage,
            StreamObserver<RobotMessage> responseObserver,
            boolean isResponse
    ) {
        final String msg = robotMessage.getMessage();
        System.out.println("[IN] From ("
                + robotMessage.getSenderId()
                + ": "
                + robotMessage.getSenderPort()
                + "): "
                + msg);

        // Compare and increase the local timestamp with the one received
        int newTimestamp = timestamp.compareAndIncreaseTimestamp(robotMessage.getTimestamp());

        try {
            switch (msg) {
                case Constants.HELLO:
                    // Saving robot in local list
                    deployedRobots.insertRobot(new CleaningRobot(
                            robotMessage.getSenderId(),
                            robotMessage.getSenderPort(),
                            Constants.SERVER_ADDR,
                            robotMessage.getStartingPosX(),
                            robotMessage.getStartingPosY()
                    ));

                    System.out.println("[HELLO] Acknowledged robot with port " +
                            robotMessage.getSenderPort() + " and ID " + robotMessage.getSenderId());

                    if (!isResponse) {
                        // Ack response
                        responseObserver.onNext(buildMessage(Constants.HELLO, newTimestamp));
                    }
                    break;
                case Constants.QUIT:
                    deployedRobots.deleteRobot(robotMessage.getSenderId());
                    //server.shutdown();

                    System.out.println("[QUIT] Acknowledged that robot with ID "
                            + robotMessage.getSenderId() + " has quit Greenfield.");
                    break;
                case Constants.NEED_MECHANIC:
                    if (
                            Mechanic.getInstance().isNeedsFix() &&
                                    robotMessage.getSenderId() != parentRobot.getId()
                    ) {
                        // If this robot wants the mechanic too, compares the timestamp and chooses
                        int queuePos = Mechanic.getInstance().addRobotToMechanicRequests(new MechanicRequest(
                                robotMessage.getSenderId(),
                                robotMessage.getTimestamp()
                        ));

                        // If the one that requested has the priority, send OK
                        if (queuePos == 0) {
                            responseObserver.onNext(buildMessage(Constants.MECHANIC_OK, newTimestamp));
                        }
                    } else {
                        // If not interested, directly replies with OK
                        responseObserver.onNext(buildMessage(Constants.MECHANIC_OK, newTimestamp));
                    }
                    break;
                case Constants.MECHANIC_OK:
                    Mechanic.getInstance().acknowledgeOK();
                    break;
                case Constants.MECHANIC_RELEASE:
                    Mechanic.getInstance().notifyForMechanicRelease(robotMessage.getSenderId());
                    break;
                case Constants.PING:
                    responseObserver.onNext(buildMessage(Constants.PONG, newTimestamp));
                    break;
                case Constants.PONG:
                    break;
                default:
                    // Crashed robot message - crash_{id}
                    try {
                        int crashedRobot = Integer.parseInt(robotMessage.getMessage().split("_")[1]);
                        deployedRobots.deleteRobot(crashedRobot);
                        System.out.println("[CRASH] Acknowledged that robot with ID "
                                + crashedRobot + " has crashed.");
                    } catch (Exception e) {
                        throw new UnrecognisedMessageException(msg);
                    }
            }
        } catch (UnrecognisedMessageException e) {
            e.printStackTrace();
        }
    }

    @Override
    public StreamObserver<RobotMessage> rcs(StreamObserver<RobotMessage> responseObserver) {

        return new StreamObserver<RobotMessage>() {

            public void onNext(RobotMessage robotMessage) {
                handleRobotMessage(robotMessage, responseObserver, false);
            }

            public void onError(Throwable throwable) {
            }

            public void onCompleted() {
            }
        };
    }
}
