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
        int senderId = robotMessage.getSenderId();
//        System.out.println("[IN] From ("
//                + senderId
//                + ": "
//                + robotMessage.getSenderPort()
//                + "): "
//                + msg);

        // Compare and increase the local timestamp with the one received
        int newTimestamp = timestamp.compareAndIncreaseTimestamp(robotMessage.getTimestamp());

        final String[] msgParts = msg.split("_");

        try {
            switch (msgParts[0]) {
                case Constants.HELLO:
                    System.out.println("[HELLO] Acknowledged robot with port " +
                            robotMessage.getSenderPort() + " and ID " + senderId);
                    if (!isResponse) {
                        // Saving robot in local list
                        deployedRobots.insertRobot(new CleaningRobot(
                                senderId,
                                robotMessage.getSenderPort(),
                                Constants.SERVER_ADDR,
                                robotMessage.getStartingPosX(),
                                robotMessage.getStartingPosY()
                        ));

//                        System.out.println("[HELLO] Acknowledged robot with port " +
//                                robotMessage.getSenderPort() + " and ID " + senderId);

                        // Ack response
                        responseObserver.onNext(buildMessage(Constants.HELLO, newTimestamp));
                    }
                    break;
                case Constants.QUIT:
                    deployedRobots.deleteRobot(senderId);
                    System.out.println("[QUIT] Acknowledged that robot with ID "
                            + senderId + " has quit Greenfield.");
                    break;
                case Constants.NEED_MECHANIC:
                    if (senderId == parentRobot.getId()) {
                        System.out.println("[MECHANIC] SELF REQUEST");
//                        Mechanic.getInstance().addRobotToMechanicRequests(new MechanicRequest(
//                                senderId,
//                                robotMessage.getTimestamp()
//                        ));
                        responseObserver.onNext(buildMessage(Constants.MECHANIC_OK, newTimestamp));
                        break;
                    } else {
                        System.out.println("[MECHANIC] Robot " + senderId + " requested mechanic.");
                    }

                    if (!Mechanic.getInstance().isNeedsFix() && !StartCleaningRobot.healthCheckThread.isRepairing()) {
                        responseObserver.onNext(buildMessage(Constants.MECHANIC_OK, newTimestamp));
                    } else if (StartCleaningRobot.healthCheckThread.isRepairing()) {
                        Mechanic.getInstance().addRobotToMechanicRequests(new MechanicRequest(
                                senderId,
                                robotMessage.getTimestamp()
                        ));
                        System.out.println("[MECHANIC] Queued request: " + Mechanic.getInstance().toString());
                    } else if (Mechanic.getInstance().isNeedsFix() && !StartCleaningRobot.healthCheckThread.isRepairing()) {
//                        int queuePos = Mechanic.getInstance().addRobotToMechanicRequests(new MechanicRequest(
//                                senderId,
//                                robotMessage.getTimestamp()
//                        ));

                        System.out.println("RequestQueue before: " + Mechanic.getInstance().toString());

                        if (Mechanic.getInstance().isFirst(robotMessage.getTimestamp())) {
                            // If the one that requested has the priority, send OK
                            responseObserver.onNext(buildMessage(Constants.MECHANIC_OK, newTimestamp));
                            // And decrease the received OKs (this other robot has now the priority)
                            Mechanic.getInstance().decreaseReceivedOKs();
                        } else {
                            // Otherwise, queue the request
                            Mechanic.getInstance().addRobotToMechanicRequests(new MechanicRequest(
                                    senderId,
                                    robotMessage.getTimestamp()
                            ));
                        }

                        System.out.println("RequestQueue after: " + Mechanic.getInstance().toString());
                    }

//                    if (!Mechanic.getInstance().isNeedsFix() /*&& !StartCleaningRobot.healthCheckThread.isRepairing()*/) {
//                        responseObserver.onNext(buildMessage(Constants.MECHANIC_OK, newTimestamp));
//                    } else if (Mechanic.getInstance().isNeedsFix()) {
//                        int queuePos = Mechanic.getInstance().addRobotToMechanicRequests(new MechanicRequest(
//                                senderId,
//                                robotMessage.getTimestamp()
//                        ));
//
//                        System.out.println("Mechanic before: " + Mechanic.getInstance().toString());
//
//                        if (!StartCleaningRobot.healthCheckThread.isRepairing()) {
//                            // If the one that requested has the priority, send OK
//                            if (queuePos == 0) {
//                                responseObserver.onNext(buildMessage(Constants.MECHANIC_OK, newTimestamp));
//                            }
//                        }
//                    }

//                    if (
//                            Mechanic.getInstance().isNeedsFix() /*&&
//                                    senderId != parentRobot.getId()*/
//                    ) {
//
//                        //System.out.println("Mechanic before: " + Mechanic.getInstance().toString());
//
//                        // If this robot wants the mechanic too, compares the timestamp and chooses
//                        int queuePos = Mechanic.getInstance().addRobotToMechanicRequests(new MechanicRequest(
//                                senderId,
//                                robotMessage.getTimestamp()
//                        ));
//
//                        System.out.println("Mechanic after: " + Mechanic.getInstance().toString());
//
//                        // If the one that requested has the priority, send OK
//                        if (queuePos == 0) {
//                            responseObserver.onNext(buildMessage(Constants.MECHANIC_OK, newTimestamp));
//                        }
//                    } else {
//                        // If not interested, directly replies with OK
//                        responseObserver.onNext(buildMessage(Constants.MECHANIC_OK, newTimestamp));
//                    }
                    break;
                case Constants.MECHANIC_OK:
                    if (senderId == parentRobot.getId()) {
                        System.out.println("[MECHANIC] SELF OK");
                    } else {
                        System.out.println("[MECHANIC] OK from " + senderId);
                    }
                    Mechanic.getInstance().acknowledgeOK();
                    break;
                case Constants.MECHANIC_RELEASE:
                    if (senderId == parentRobot.getId()) {
                        System.out.println("[MECHANIC] SELF RELEASE");
                    } else {
                        System.out.println("[MECHANIC] Released from " + senderId);
                    }
                    Mechanic.getInstance().notifyForMechanicRelease(senderId);
                    break;
                case Constants.PING:
                    responseObserver.onNext(buildMessage(Constants.PONG, newTimestamp));
                    break;
                case Constants.PONG:
                    break;
                case Constants.CRASH:
                    // Msg format: crash_{crashedRobotId}
                    int crashedRobot = Integer.parseInt(msgParts[1]);
                    deployedRobots.deleteRobot(crashedRobot);
                    System.out.println("[CRASH] Acknowledged that robot with ID "
                            + crashedRobot + " has crashed.");
                    break;
                case Constants.CHANGE_DISTRICT:
                    // Msg format: changeDistrict_{movedRobotId}_{newPosX}_{newPosY}
                    final int movedRobotId = Integer.parseInt(msgParts[1]);
                    final int newPosX = Integer.parseInt(msgParts[2]);
                    final int newPosY = Integer.parseInt(msgParts[3]);
                    deployedRobots.changeRobotDistrict(movedRobotId, newPosX, newPosY);

                    if (parentRobot.getId() == movedRobotId) {
                        // If this robot is the one that got moved, update its data
                        parentRobot.setPosX(newPosX);
                        parentRobot.setPosY(newPosY);
                        // Tell the server the new position
                        StartCleaningRobot.updatePosition(newPosX, newPosY);
                    }

                    System.out.println("[CRASH] Acknowledged that robot with ID "
                            + movedRobotId + " has changed its position to [" + newPosX + ", " + newPosY + "].");
                    break;
                default:
                    throw new UnrecognisedMessageException(msgParts[0]);
            }
        } catch (UnrecognisedMessageException e) {
            System.err.println("[RCS] Unrecognised message: " + e);
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
