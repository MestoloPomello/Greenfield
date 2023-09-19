package cleaning_robot;

import cleaning_robot.proto.RobotCommunicationServiceGrpc.*;
import cleaning_robot.proto.RobotMessageOuterClass.*;
import io.grpc.stub.StreamObserver;
import shared.beans.CleaningRobot;
import shared.constants.Constants;
import shared.exceptions.UnrecognisedMessageException;

import java.util.List;

public class RobotCommunicationServiceImpl extends RobotCommunicationServiceImplBase {

    List<CleaningRobot> deployedRobots;

    public RobotCommunicationServiceImpl() { super(); }

    public RobotCommunicationServiceImpl(List<CleaningRobot> deployedRobots) {
        super();
        this.deployedRobots = deployedRobots;
    }

    @Override
    public StreamObserver<RobotMessage> rcs(StreamObserver<RobotMessage> responseObserver) {

        return new StreamObserver<RobotMessage>() {

            public void onNext(RobotMessage robotMessage) {

                final String msg = robotMessage.getMessage();
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

                            System.out.println("> HELLO: acknowledged robot with port " +
                                    robotMessage.getSenderPort() + "and ID " + robotMessage.getSenderId());

                            // Ack response
                            responseObserver.onNext(RobotMessage.newBuilder().setMessage(Constants.HELLO).build());
                            break;
                        case Constants.QUIT:

                            break;
                        case Constants.REQ_MECHANIC:
                            break;
                        default:
                            throw new UnrecognisedMessageException(msg);
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
