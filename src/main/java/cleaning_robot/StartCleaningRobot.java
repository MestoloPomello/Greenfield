package cleaning_robot;

import cleaning_robot.proto.RobotCommunicationServiceGrpc;
import cleaning_robot.proto.RobotCommunicationServiceGrpc.RobotCommunicationServiceStub;
import cleaning_robot.proto.RobotMessageOuterClass.RobotMessage;
import cleaning_robot.threads.HealthCheckThread;
import cleaning_robot.threads.InputThread;
import cleaning_robot.threads.SensorThread;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import shared.beans.BufferImpl;
import shared.beans.CleaningRobot;
import shared.beans.InputRobot;
import shared.beans.RobotCreationResponse;
import shared.constants.Constants;
import shared.exceptions.DuplicatedIdException;
import shared.utils.LamportTimestamp;
import simulators.Buffer;
import simulators.PM10Simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import static shared.utils.Utils.getRandomInt;


public class StartCleaningRobot {

    public static String serverAddress = "http://" + Constants.SERVER_ADDR + ":" + Constants.SERVER_PORT;
    private static int id, posX, posY, portNumber;
    private static int district;
    private static List<CleaningRobot> deployedRobots;
    private static LamportTimestamp timestamp;
    private static CleaningRobot selfReference;
    private static Client client;

    public static void main(String[] args) {

        portNumber = getRandomInt(49152, 65535); // Following IANA guidelines

        // Lamport clock
        timestamp = new LamportTimestamp();

        try {
            client = Client.create();

            Scanner scanner = new Scanner(System.in);
            System.out.print("> Insert the ID for this robot: ");
            id = Integer.parseInt(scanner.nextLine());

            // The robot calls the server in order to register itself
            InputRobot newRobot = new InputRobot(id, portNumber, Constants.SERVER_ADDR);
            timestamp.increaseTimestamp();
            RobotCreationResponse response = postNewRobot(newRobot);

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
            broadcastMessage(Constants.HELLO);

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
            InputThread inputThread = new InputThread(
                    selfReference,
                    client,
                    simulator,
                    sensorThread);
            inputThread.start();

            // Opens listening connection for other robots
            createNewRCS(deployedRobots);

            // Start health check thread
            HealthCheckThread healthCheck = new HealthCheckThread(
                    selfReference,
                    deployedRobots
            );
            healthCheck.start();

        } catch (DuplicatedIdException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Unknown error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static RobotCreationResponse postNewRobot(InputRobot newRobot){
        WebResource webResource = client.resource(serverAddress + "/robot");
        try {
            return webResource.type("application/json").post(RobotCreationResponse.class, new Gson().toJson(newRobot));
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
            return null;
        }
    }

    public static void notifyRobotCrash(int robotId) {
        // Tells the server that a robot crashed, then broadcasts the info to other robots
        WebResource webResource = client.resource(serverAddress + "/robot/crash/" + robotId);
        try {
            webResource.type("application/json").delete(String.class);
            broadcastMessage("crash_" + robotId);
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
        }
    }

    public static ClientResponse getRequest(String url){
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
            return null;
        }
    }

    public static String deleteRequest(String url){
        WebResource webResource = client.resource(url);
        try {

            return webResource.type("application/json").delete(String.class);
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
            return null;
        }
    }

    public static void createNewRCS (List<CleaningRobot> deployedRobots) {
        List<CleaningRobot> deployedRobotsWithoutSelf = new ArrayList<>(deployedRobots);
        deployedRobotsWithoutSelf.remove(selfReference);
        try {
            RobotCommunicationServiceImpl service = new RobotCommunicationServiceImpl(
                    selfReference,
                    deployedRobotsWithoutSelf,
                    timestamp
            );
            io.grpc.Server server = ServerBuilder
                    .forPort(portNumber)
                    .addService(service)
                    .build();
            service.setServer(server);
            server.start();
            System.out.println("[RCS] Opened listening service for other robots.");
            server.awaitTermination();
        } catch (IOException e) {
            System.err.println("[RCS] IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("[RCS] InterruptException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendMessageToOtherRobot (CleaningRobot otherRobot, String msg) {
        final ManagedChannel channel = ManagedChannelBuilder
                .forTarget(Constants.SERVER_ADDR + ":" + otherRobot.getPort())
                .usePlaintext()
                .build();
        RobotCommunicationServiceStub stub = RobotCommunicationServiceGrpc.newStub(channel);

        StreamObserver<RobotMessage> robotStream = stub.rcs(new StreamObserver<RobotMessage>() {
            public void onNext(RobotMessage robotMessage) {
                String response = robotMessage.getMessage();
                System.out.println("[IN] From ("
                        + robotMessage.getSenderId()
                        + ": "
                        + robotMessage.getSenderPort()
                        + "): "
                        + response);

                handleRobotResponse(robotMessage);
            }

            public void onError(Throwable throwable) {
                //throwable.printStackTrace();

                System.out.println("[ERROR] Robot with port " + otherRobot.getPort() + " is unreachable. Closing connection and notifying server.");

                // Delete the robot from the list
                deployedRobots.removeIf(cr -> cr.getId() == otherRobot.getId());

                // Notify the server that the robot crashed
                notifyRobotCrash(otherRobot.getId());

                // Close the channel with the crashed server
                channel.shutdown();
            }

            public void onCompleted() {
            }
        });

        try {
            timestamp.increaseTimestamp();
            System.out.println("[OUT] To "
                    + otherRobot.getPort()
                    + " "
                    + otherRobot.getId()
                    + " | TS: "
                    + timestamp.getTimestamp()
                    + " | Msg: " + msg);
            robotStream.onNext(RobotMessage.newBuilder()
                    .setSenderId(id)
                    .setSenderPort(otherRobot.getPort())
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

    public static void broadcastMessage(String message) {
        // Sends a message to all of the other robots
        ExecutorService executor = Executors.newFixedThreadPool(deployedRobots.size());

        for (CleaningRobot otherRobot : deployedRobots) {
            if (otherRobot.getId() != id) { // Only consider other robots
                executor.submit(() -> sendMessageToOtherRobot(otherRobot, message));
            } else {
                selfReference = otherRobot;
            }
        }

        executor.shutdown();
        try {
            // Timeout for execution
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void handleRobotResponse(RobotMessage message) {

        // Lamport algorithm for the message's timestamp
        timestamp.compareAndIncreaseTimestamp(message.getTimestamp());

        // Handle the response's content
//        String content = message.getMessage();
//        switch (content) {
//            case Constants.PONG:
//
//                break;
//            default:
//                break;
//        }
    }
}

