// Il server si deve connettere al MQTT broker per leggere i dati inviati dai robot
/*
    Il server REST Jersey deve gestire:
    - inserimento e rimozione di robots dalla lista
    - un'insieme di statistiche che possono essere lette dall'AdministratorClient
 */

package administrator_server;

import administrator_server.beans.ServerMeasurement;
import administrator_server.beans.Measurements;
import com.google.common.reflect.TypeToken;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import io.grpc.Server;
import org.eclipse.paho.client.mqttv3.*;

import shared.beans.AveragesPayload;
import shared.constants.Constants;
import shared.utils.LamportTimestamp;
import simulators.Measurement;

public class StartServer {

    static LamportTimestamp timestamp;

    public static void main(String[] args) throws IOException {

        // To remove the red wall of text from Jersey
        Logger.getLogger( "com" ).setLevel( Level.SEVERE );

        // Lamport clock
        timestamp = new LamportTimestamp();

        // Server connection
        HttpServer server = HttpServerFactory.create("http://" + Constants.SERVER_ADDR + ":" + Constants.SERVER_PORT + "/");
        server.start();
        System.out.println("Server started on: http://" + Constants.SERVER_ADDR + ":" + Constants.SERVER_PORT);

        // MQTT Broker connection
        String clientId = MqttClient.generateClientId();

        try {
            MqttClient client = new MqttClient(Constants.BROKER_ADDR, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            System.out.println(clientId + "\n> Connecting Broker " + Constants.BROKER_ADDR);
            client.connect(connOpts);
            System.out.println(clientId + "\n> Connected - Thread PID: " + Thread.currentThread().getId());

            client.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) {
                    Gson gson = new Gson();

//                    Type measurementType = new TypeToken<ArrayList<ServerMeasurement>>(){}.getType();
//                    ArrayList<ServerMeasurement> newMeasurements = gson.fromJson(new String(message.getPayload()), measurementType);

                    AveragesPayload averagesData = gson.fromJson(new String(message.getPayload()), AveragesPayload.class);
                    List<Integer> insertsResponses = new ArrayList<>();
                    StringBuilder strAverages = new StringBuilder();

                    for (Measurement average : averagesData.getAverages()) {
                        ServerMeasurement newMeasurement = new ServerMeasurement(
                                Character.getNumericValue(topic.charAt(topic.length() - 1)),
                                Integer.toString(averagesData.getRobotID()),
                                average.getType(),
                                average.getValue(),
                                averagesData.getTimestamp()
                        );

                        strAverages.append(average.getValue()).append(", ");
                        insertsResponses.add(Measurements.getInstance().insertMeasurement(newMeasurement));
                    }

                    // Remove last ", "
                    strAverages.delete(strAverages.length() - 2, strAverages.length());

                    System.out.println("[PM10] New measurements received - Thread PID: " + Thread.currentThread().getId() +
                            "\n\tTopic: " + topic +
                            "\n\tQoS: " + message.getQos() +
                            "\n\tTimestamp: " + averagesData.getTimestamp() +
                            "\n\tPM10 averages: " + strAverages + "\n");

                }

                public void connectionLost(Throwable c) {
                    System.err.println("[ERROR] Connection lost. Reason: " + c.getMessage() + " - Thread PID: " + Thread.currentThread().getId());
                    c.printStackTrace();
                }

                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
            client.subscribe(Constants.TOPICS, Constants.QOS);
            for (String topic : Constants.TOPICS) {
                System.out.println("[TOPIC] Subscribed to topic: " + topic);
            }
        } catch (MqttException e) {
            System.err.println("[ERROR] Failed connection to the MQTT Broker: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Error in the MQTT subscription: " + e.getMessage());
        }

//        System.out.println("Hit return to stop...");
//        System.in.read();
//        System.out.println("Stopping server");
//        server.stop(0);
//        System.out.println("Server stopped");
    }
}
