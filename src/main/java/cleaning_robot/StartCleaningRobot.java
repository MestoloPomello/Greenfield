package cleaning_robot;

import cleaning_robot.proto.RobotCommunicationServiceGrpc;
import cleaning_robot.proto.RobotCommunicationServiceGrpc.*;
import cleaning_robot.proto.RobotMessageOuterClass.*;
import cleaning_robot.threads.InputThread;
import cleaning_robot.threads.SensorThread;
import com.google.gson.Gson;
import io.grpc.ServerBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import shared.beans.BufferImpl;
import shared.constants.Constants;
import shared.beans.RobotCreationResponse;
import shared.beans.InputRobot;
import shared.beans.CleaningRobot;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import shared.exceptions.DuplicatedIdException;

import shared.utils.LamportTimestamp;
import simulators.Buffer;
import simulators.PM10Simulator;

import static shared.utils.Utils.getRandomInt;


public class StartCleaningRobot {

    static int id, posX, posY, portNumber;
    static int district;
    static List<CleaningRobot> deployedRobots;
    static LamportTimestamp timestamp;
    static CleaningRobot selfReference;

    public static void main(String[] args) throws IOException {

        portNumber = getRandomInt(49152, 65535); // Following IANA guidelines

        // Lamport clock
        timestamp = new LamportTimestamp();

        try {
            Client client = Client.create();
            String serverAddress = "http://" + Constants.SERVER_ADDR + ":" + Constants.SERVER_PORT;

            Scanner scanner = new Scanner(System.in);
            System.out.print("> Insert the ID for this robot: ");
            id = Integer.parseInt(scanner.nextLine());

            // The robot calls the server in order to register itself
            InputRobot newRobot = new InputRobot(id, portNumber, Constants.SERVER_ADDR);
            timestamp.increaseTimestamp();
            RobotCreationResponse response = postRequest(client, serverAddress + "/robot", newRobot);

            if (response == null) {
                throw new Exception("Null robot creation response");
            }

            switch (response.getStatus()) {
                case Constants.STATUS_SUCCESS:
                    posX = response.getPosX();
                    posY = response.getPosY();
                    deployedRobots = response.getRegisteredRobots();
                    district = response.getDistrictFromPos();

                    System.out.println("[SUCCESS] New robot accepted from the server." +
                            "\n\tID: " + id +
                            "\n\tDistrict: " + district +
                            "\n\tStarting PosX: " + posX +
                            "\n\tStarting PosY: " + posY +
                            "\n\tPort: " + portNumber + "\n");
                    break;
                case Constants.ERR_DUPED_ID:
                    throw new DuplicatedIdException("Couldn't register this new robot");
                default:
                    throw new Exception("Unknown status");
            }

            // Presents itself to other robots
            for (CleaningRobot otherRobot : deployedRobots) {
                if (otherRobot.getId() != id) { // Only consider other robots
                    sendMessageToOtherRobot(otherRobot.getPort(), Constants.HELLO);
                } else {
                    selfReference = otherRobot;
                }
            }

            // Create objects
            Buffer buffer = new BufferImpl();
            PM10Simulator simulator = new PM10Simulator(buffer);
            SensorThread sensorThread = new SensorThread(
                    buffer,
                    district,
                    id,
                    timestamp);

            // Start PM10 measurements
            simulator.start();
            sensorThread.start();

            // Awaits for inputs from the administrator
            InputThread inputThread = new InputThread(id);
            inputThread.start();

            // Opens listening connection for other robots
            createNewRCS(deployedRobots);

        } catch (DuplicatedIdException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Unknown error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static RobotCreationResponse postRequest(Client client, String url, InputRobot newRobot){
        System.out.println("URL: " + url);
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").post(RobotCreationResponse.class, new Gson().toJson(newRobot));
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
            return null;
        }
    }

    public static ClientResponse getRequest(Client client, String url){
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
            return null;
        }
    }


    public static void createNewRCS (List<CleaningRobot> deployedRobots) {
        try {
            RobotCommunicationServiceImpl service = new RobotCommunicationServiceImpl(
                    selfReference,
                    deployedRobots,
                    timestamp
            );
            io.grpc.Server server = ServerBuilder
                    .forPort(portNumber)
                    .addService(service)
                    .build();
            service.setServer(server);
            server.start();
            System.out.println("> Robot's RCS: opened listening service for other robots.");
            server.awaitTermination();
        } catch (IOException e) {
            System.err.println("[createNewRCS] IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("[createNewRCS] InterruptException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendMessageToOtherRobot (int otherRobotPort, String msg) {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(Constants.SERVER_ADDR + ":" + otherRobotPort).usePlaintext().build();
        RobotCommunicationServiceStub stub = RobotCommunicationServiceGrpc.newStub(channel);

        StreamObserver<RobotMessage> robotStream = stub.rcs(new StreamObserver<RobotMessage>() {
            public void onNext(RobotMessage robotMessage) {

                System.out.println("> Response received from robot with port " + otherRobotPort + ": " + robotMessage.getMessage());

                // Response handling is based on the type of message sent
//                switch (msg) {
//                    case Constants.HELLO:
//
//                        break;
//                    case Constants.QUIT:
//                        break;
//                    case Constants.REQ_MECHANIC:
//                        break;
//                }
                //System.out.println("[FROM SERVER] " + robotMessage.getStringResponse());
            }

            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            public void onCompleted() {
            }
        });

        timestamp.increaseTimestamp();

        System.out.println("> Message sent."
                + "\n\tReceiver port: " + otherRobotPort
                + "\n\tTimestamp: " + timestamp.getTimestamp()
                + "\n\tMessage: " + msg);

        try {
            robotStream.onNext(RobotMessage.newBuilder()
                    .setSenderId(id)
                    .setSenderPort(otherRobotPort)
                    .setTimestamp(timestamp.getTimestamp())
                    .setStartingPosX(posX)
                    .setStartingPosY(posY)
                    .setMessage(msg)
                    .build());
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }

        try {
            channel.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

