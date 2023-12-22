package cleaning_robot.threads;

import cleaning_robot.StartCleaningRobot;
import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import shared.beans.AveragesPayload;
import shared.constants.Constants;
import simulators.Buffer;
import static cleaning_robot.StartCleaningRobot.timestamp;
import static cleaning_robot.StartCleaningRobot.district;
import static cleaning_robot.StartCleaningRobot.id;

import java.util.List;

import org.eclipse.paho.client.mqttv3.*;
import simulators.Measurement;

public class SensorThread extends Thread {

    private volatile boolean running = true;

    private final Buffer buffer;
    private MqttClient client;
    private final String clientId;
    private boolean isUnderReparation;

    public SensorThread(Buffer buffer) {
        this.buffer = buffer;
        clientId = MqttClient.generateClientId();
        isUnderReparation = false;
    }

    @Override
    public void run() {

        MqttClient client = setupMqttClient();
        if (client == null) {
            running = false;
            System.err.println("[ERROR] SetupMqttClient returned null.");
            return;
        }

        while (running) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (isUnderReparation) {
                try {
                    StartCleaningRobot.class.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            Gson gson = new Gson();
            List<Measurement> averages = buffer.readAllAndClean();

            timestamp.increaseTimestamp();

            AveragesPayload ap = new AveragesPayload(
                    averages,
                    id,
                    timestamp.getTimestamp()
            );

            String payload = gson.toJson(ap);

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(Constants.QOS[district - 1]);
            try {
                client.publish(Constants.TOPICS[district - 1], message);
                System.out.println("[MEASUREMENT] TS: " + timestamp.getTimestamp() + " | Averages successfully published on the MQTT broker.");
            } catch (MqttException e) {
                if (!running) {
                    System.out.println("[MEASUREMENT] Stopped publishing measurements.");
                } else {
                    System.err.println("[ERROR] Failed while publishing the measurement on MQTT broker.");
                    e.printStackTrace();
                }
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
            System.out.println("[MQTT] Successfully connected to the MQTT broker.");
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

    public void setInReparation() {
        isUnderReparation = true;
    }

    public void setReparationEnded() {
        isUnderReparation = false;
    }
}
