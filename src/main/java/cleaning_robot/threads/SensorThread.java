package cleaning_robot.threads;

import administrator_server.beans.Measurements;
import administrator_server.beans.ServerMeasurement;
import com.google.gson.Gson;
import io.grpc.Server;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import shared.constants.Constants;
import simulators.Buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.*;
import simulators.Measurement;

public class SensorThread extends Thread {

    private volatile boolean running = true;

    private final Buffer buffer;
    private MqttClient client;
    private final String clientId;
    private final int district;

    public SensorThread(Buffer buffer, int district) {
        this.buffer = buffer;
        this.district = district;
        clientId = MqttClient.generateClientId();
    }

    @Override
    public void run() {

        MqttClient client = setupMqttClient();
        if (client == null) {
            running = false;
            System.err.println("SetupMqttClient returned null.");
            return;
        }

        while (running) {
            Gson gson = new Gson();

            List<Measurement> bufferedMeasurements = buffer.readAllAndClean();
            String payload = gson.toJson(bufferedMeasurements);
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(Constants.QOS[district - 1]);
            try {
                client.publish(Constants.TOPICS[district - 1], message);
                System.out.println("Measurements successfully published on the MQTT broker.");
            } catch (MqttException e) {
                System.err.println("[ERROR] Failed while publishing the measurement on MQTT broker.");
                e.printStackTrace();
            }

            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        disconnectMqttClient();
    }

    public void stopThread() {
        running = false;
        interrupt();
    }

    private MqttClient setupMqttClient() {
        try {
            client = new MqttClient(Constants.BROKER_ADDR, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            System.out.println("Successfully connected to the MQTT broker.");
        } catch (MqttException e) {
            System.out.println("[ERROR] Couldn't connect to the MQTT broker.");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.out.println("[ERROR] Unknown error.");
            e.printStackTrace();
            return null;
        }
        return client;
    }

    private void disconnectMqttClient() {
        // After the thread stops, disconnect it from the MQTT broker
        if (client.isConnected()) {
            try {
                client.disconnect();
                System.out.println("SensorThread disconnected from the MQTT Broker.");
            } catch (MqttException e) {
                System.out.println("[ERROR] Couldn't disconnect from the MQTT broker.");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("[ERROR] Unknown error.");
                e.printStackTrace();
            }
        }
    }

}
