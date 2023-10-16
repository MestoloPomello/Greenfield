package cleaning_robot.threads;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import shared.beans.AveragesPayload;
import shared.constants.Constants;
import shared.utils.LamportTimestamp;
import simulators.Buffer;

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
    private final int robotID;
    private final LamportTimestamp timestamp;

    public SensorThread(Buffer buffer, int district, int robotID, LamportTimestamp timestamp) {
        this.buffer = buffer;
        this.district = district;
        this.robotID = robotID;
        this.timestamp = timestamp;
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
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Gson gson = new Gson();
            List<Measurement> averages = buffer.readAllAndClean();

            timestamp.increaseTimestamp();

            AveragesPayload ap = new AveragesPayload(
                    averages,
                    robotID,
                    timestamp.getTimestamp()
            );

            String payload = gson.toJson(ap);

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(Constants.QOS[district - 1]);
            try {
                client.publish(Constants.TOPICS[district - 1], message);
                System.out.println("[SensorThread] Measurement averages successfully published on the MQTT broker.");
            } catch (MqttException e) {
                System.err.println("[ERROR] Failed while publishing the measurement on MQTT broker.");
                e.printStackTrace();
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
