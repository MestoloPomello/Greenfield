// Il server si deve connettere al MQTT broker per leggere i dati inviati dai robot
/*
    Il server REST Jersey deve gestire:
    - inserimento e rimozione di robots dalla lista
    - un'insieme di statistiche che possono essere lette dall'AdministratorClient
 */

package administrator_server;

import administrator_server.beans.ServerMeasurement;
import administrator_server.beans.Measurements;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.*;

import shared.constants.Constants;

public class StartServer {

    public static void main(String[] args) throws IOException {

        // To remove the red wall of text from Jersey
        Logger.getLogger( "com" ).setLevel( Level.SEVERE );

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
                    ServerMeasurement newMeasurement = gson.fromJson(new String(message.getPayload()), ServerMeasurement.class);
                    newMeasurement.setDistrict(Character.getNumericValue(topic.charAt(topic.length() - 1)));

                    System.out.println("[PM10] New measurement received - Thread PID: " + Thread.currentThread().getId() +
                            "\n\tTopic: " + topic +
                            "\n\tQoS: " + message.getQos() +
                            "\n\tTimestamp: " + newMeasurement.getTimestamp() +
                            "\n\tPM10: " + newMeasurement.getValue() + "\n");

                    int response = Measurements.getInstance().insertMeasurement(newMeasurement);
                }

                public void connectionLost(Throwable c) {
                    System.err.println("[ERROR] Connection lost. Reason: " + c.getMessage()+ " -  Thread PID: " + Thread.currentThread().getId());
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
