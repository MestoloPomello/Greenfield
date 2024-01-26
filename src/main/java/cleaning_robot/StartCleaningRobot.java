package cleaning_robot;

import cleaning_robot.beans.DeployedRobots;
import cleaning_robot.beans.Mechanic;
import cleaning_robot.proto.RobotCommunicationServiceGrpc;
import cleaning_robot.proto.RobotCommunicationServiceGrpc.RobotCommunicationServiceStub;
import cleaning_robot.proto.RobotMessageOuterClass.RobotMessage;
import cleaning_robot.threads.*;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import shared.beans.*;
import shared.constants.Constants;
import shared.exceptions.DuplicatedIdException;
import shared.utils.LamportTimestamp;
import simulators.Buffer;
import simulators.PM10Simulator;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static shared.utils.Utils.generateCoordinatesForDistrict;
import static shared.utils.Utils.getRandomInt;


public class StartCleaningRobot {

    public static String serverAddress = "http://" + Constants.SERVER_ADDR + ":" + Constants.SERVER_PORT;
    public static int id, posX, posY, portNumber;
    public static int district;
    public static LamportTimestamp timestamp;
    public static DeployedRobots deployedRobots;
    public static CleaningRobot selfReference;
    public static Client client;

    // Threads
    public static RcsThread rcsThread;
    public static SensorThread sensorThread;
    public static InputThread inputThread;
    public static PingThread pingThread;
    public static HealthCheckThread healthCheckThread;

    // Connections with robots
    private static final Map<Integer, StreamObserver<RobotMessage>> streamsMap = new HashMap<>();


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
                    deployedRobots = new DeployedRobots(response.getRegisteredRobots());
                    district = response.getDistrictFromPos();
                    selfReference = deployedRobots.getSelfReference(id);

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

            // Open listening connection for other robots
            rcsThread = new RcsThread();
            rcsThread.start();

            // Presents itself to other robots
            broadcastMessage_All(Constants.HELLO, false);

            // Create objects
            Buffer buffer = new BufferImpl();
            PM10Simulator simulator = new PM10Simulator(buffer);
            sensorThread = new SensorThread(buffer);

            // Start PM10 measurements
            simulator.start();
            sensorThread.start();

            // Awaits for inputs from the administrator
            inputThread = new InputThread(simulator);
            inputThread.start();

            // Start ping thread (checking if the next robot is alive)
            pingThread = new PingThread();
            pingThread.start();

            // Starts health check thread
            healthCheckThread = new HealthCheckThread();
            healthCheckThread.start();

        } catch (DuplicatedIdException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Unknown error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static RobotCreationResponse postNewRobot(InputRobot newRobot) {
        WebResource webResource = client.resource(serverAddress + "/robot");
        try {
            return webResource.type("application/json").post(RobotCreationResponse.class, new Gson().toJson(newRobot));
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
            return null;
        }
    }

    public static void updatePosition(int newPosX, int newPosY) {
        WebResource webResource = client.resource(serverAddress + "/robot/" + id + "/" + newPosX + "-" + newPosY);
        try {
            webResource.type("application/json").put();
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
        }
    }

    public static void notifyRobotCrash(int robotId) {
        // Tells the server that a robot crashed, then broadcasts the info to other robots
        WebResource webResource = client.resource(serverAddress + "/robot/crash/" + robotId);
        try {
            webResource.type("application/json").delete(String.class);
            broadcastMessage_All("crash_" + robotId, false);
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
        }
    }

    public static ClientResponse getRequest(String url) {
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
            return null;
        }
    }

    public static void deleteRequest(String url) {
        WebResource webResource = client.resource(url);
        try {
            webResource.type("application/json").delete(String.class);
        } catch (ClientHandlerException e) {
            System.err.println("[ERROR] Unreachable server.");
        }
    }

    public static void sendMessageToOtherRobot(CleaningRobot otherRobot, String msg) {

        int otherRobotPort = otherRobot.getPort();
        StreamObserver<RobotMessage> robotStream = null;
        synchronized (streamsMap) {
            robotStream = streamsMap.get(otherRobotPort);
        }

        if (robotStream == null) {
            final ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(Constants.SERVER_ADDR + ":" + otherRobotPort)
                    .usePlaintext()
                    .build();

            RobotCommunicationServiceStub stub = RobotCommunicationServiceGrpc.newStub(channel);
            robotStream = stub.rcs(new StreamObserver<RobotMessage>() {
                public void onNext(RobotMessage robotMessage) {
                    rcsThread.service.handleRobotMessage(robotMessage, this, true);
                }

                public void onError(Throwable throwable) {
                    System.out.println("[ERROR] Robot with port " + otherRobotPort + " is unreachable. Closing connection and notifying server.");

                    synchronized (streamsMap) {
                        streamsMap.remove(otherRobotPort);
                    }

                    // If this robot was awaiting for an OK from the crashed one, act as if it sent it
                    Mechanic.getInstance().notifyForMechanicRelease(otherRobot.getId());

                    // The robot that should start the district change is the one before the one that crashed
                    boolean hasToStartDistrictChange = deployedRobots.findNextRobot(selfReference).getPort() == otherRobotPort;

                    // Delete the robot from the list
                    deployedRobots.deleteRobot(otherRobot.getId());

                    if (hasToStartDistrictChange) {
                        // Notify the server that the robot crashed
                        notifyRobotCrash(otherRobot.getId());

                        // Choose the robot to be notified for the district balancing change and notify everyone about it
                        int[] districts = {0, 0, 0, 0};
                        for (CleaningRobot cr : deployedRobots.getDeployedRobots()) {
                            int robotDistrict = cr.getDistrictFromPos();
                            districts[robotDistrict - 1]++;
                        }
                        System.out.println("Distretti mappati: " + Arrays.toString(districts));
                        int max = districts[0], min = districts[0];
                        int oldDistrict = 1, newDistrict = 1;
                        for (int i = 0; i < 4; i++) {
                            if (districts[i] > max) {
                                max = districts[i];
                                oldDistrict = i + 1;
                            } else if (districts[i] < min) {
                                min = districts[i];
                                newDistrict = i + 1;
                            }
                        }
                        CleaningRobot toBeMoved = null;
                        for (CleaningRobot cr : deployedRobots.getDeployedRobots()) {
                            if (cr.getDistrictFromPos() == oldDistrict) {
                                toBeMoved = cr;
                                break;
                            }
                        }
                        System.out.println("Nuovo distretto: " + newDistrict);
                        int[] newPos = generateCoordinatesForDistrict(newDistrict);
                        System.out.println("Nuova posizione: [" + newPos[0] + ", " + newPos[1] + "]");
                        if (toBeMoved != null) broadcastMessage_All(
                                Constants.CHANGE_DISTRICT + "_"
                                        + toBeMoved.getId() + "_"
                                        + newPos[0] + "_"
                                        + newPos[1],
                                true);
                    }

                    // Close the channel with the crashed server
                    try {
                        channel.awaitTermination(1, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                public void onCompleted() {
                }
            });

            streamsMap.put(otherRobotPort, robotStream);
        }

        try {
            int newTimestamp = timestamp.increaseTimestamp();
//            System.out.println("[OUT] To "
//                    + otherRobot.getPort()
//                    + " "
//                    + otherRobot.getId()
//                    + " | TS: "
//                    + timestamp.getTimestamp()
//                    + " | Msg: " + msg);
            robotStream.onNext(RobotMessage.newBuilder()
                    .setSenderId(id)
                    .setSenderPort(portNumber)
                    .setTimestamp(newTimestamp)
                    .setStartingPosX(posX)
                    .setStartingPosY(posY)
                    .setMessage(msg)
                    .build());
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }

    }

    public static void broadcastMessage_All(String message, boolean selfBroadcast) {
        broadcastMessage(message, selfBroadcast, deployedRobots.getDeployedRobots());
    }

    public static void broadcastMessage(String message, boolean selfBroadcast, List<CleaningRobot> receivers) {
        // Sends a message to all of the other robots (parallel)
        // If selfBroadcast is true, send the msg even to itself
        List<Thread> threads = new ArrayList<>();

        for (CleaningRobot otherRobot : receivers) {
            if (otherRobot != null) {
                if (otherRobot.getId() != id || selfBroadcast) {
                    Thread thread = new Thread(() -> sendMessageToOtherRobot(otherRobot, message));
                    thread.start();
                    threads.add(thread);
                } else {
                    selfReference = otherRobot;
                }
            }
        }

        // Wait for all the threads to end
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

//    public static RobotMessage sendMessageToOtherRobot_Sync(CleaningRobot otherRobot, String msg) {
//        final ManagedChannel channel = ManagedChannelBuilder
//                .forTarget(Constants.SERVER_ADDR + ":" + otherRobot.getPort())
//                .usePlaintext()
//                .build();
//
//        RobotCommunicationService_SyncGrpc.RobotCommunicationService_SyncBlockingStub stub
//                = RobotCommunicationService_SyncGrpc.newBlockingStub(channel);
//
//        try {
//            int newTimestamp = timestamp.increaseTimestamp();
//            RobotMessage request = RobotMessage.newBuilder()
//                    .setSenderId(id)
//                    .setSenderPort(portNumber)
//                    .setTimestamp(newTimestamp)
//                    .setStartingPosX(posX)
//                    .setStartingPosY(posY)
//                    .setMessage(msg)
//                    .build();
//
//            return stub.rcsSync(request);
//        } catch (StatusRuntimeException e) {
//            e.printStackTrace();
//
//            System.out.println("[ERROR] Robot with port " + otherRobot.getPort() + " is unreachable. Closing connection and notifying server.");
//
//            // Delete the robot from the list
//            deployedRobots.deleteRobot(otherRobot.getId());
//
//            // Notify the server that the robot crashed
//            notifyRobotCrash(otherRobot.getId());
//
//            // Close the channel with the crashed server
//            channel.shutdown();
//
//            return null;
//        } finally {
//            channel.shutdown();
//            try {
//                channel.awaitTermination(1, TimeUnit.SECONDS);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    public static List<RobotMessage> syncBroadcastMessage(String message) {
//        List<Thread> threads = new ArrayList<>();
//        List<RobotMessage> responses = new ArrayList<>();
//
//        for (CleaningRobot otherRobot : deployedRobots.getDeployedRobots()) {
//            if (otherRobot != null) {
//                if (otherRobot.getId() != id) {
//                    Thread thread = new Thread(() -> {
//                        RobotMessage response = sendMessageToOtherRobot_Sync(otherRobot, message);
//                        if (response != null) {
//                            synchronized (responses) {
//                                responses.add(response);
//                            }
//                        }
//                    });
//                    thread.start();
//                    threads.add(thread);
//                } else {
//                    selfReference = otherRobot;
//                }
//            }
//        }
//
//        // Wait for all the threads to end
//        for (Thread thread : threads) {
//            try {
//                thread.join();
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        return responses;
//    }

}

