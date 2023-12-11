package administrator_server.beans;

import io.grpc.Server;
import shared.beans.ServerMeasurement;
import shared.constants.Constants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Measurements {

    @XmlElement(name="measurements_set")
    private List<ServerMeasurement> measurementsList;

    private final static Object lock = new Object();
    private static Measurements instance;

    private Measurements() { measurementsList = new ArrayList<ServerMeasurement>(); }

    // Singleton
    public static Measurements getInstance(){
        synchronized (lock) {
            if (instance == null)
                instance = new Measurements();
            return instance;
        }
    }

    public void setMeasurementsList(List<ServerMeasurement> measurementsList) {
        synchronized (lock) {
            this.measurementsList = measurementsList;
        }
    }

    public List<ServerMeasurement> getMeasurementsList() {
        // Get the whole measurements array
        synchronized (lock) {
            return new ArrayList<>(measurementsList);
        }
    }

    public List<ServerMeasurement> getMeasurementsList(int robotId) {
        // Get measurements from the given robot ID
        List<ServerMeasurement> results = new ArrayList<>();
        synchronized (lock) {
            for (ServerMeasurement sm : measurementsList) {
                if (Integer.parseInt(sm.getId()) == robotId) {
                    results.add(sm);
                }
            }
        }
        return results;
    }

    public List<ServerMeasurement> getMeasurementsList(long t1, long t2) {
        // Get measurements between two given timestamps
        List<ServerMeasurement> results = new ArrayList<>();
        synchronized (lock) {
            for (ServerMeasurement sm : measurementsList) {
                long smTs = sm.getTimestamp();
                if (smTs >= t1 && smTs <= t2) {
                    results.add(sm);
                }
            }
        }
        return results;
    }

    public int insertMeasurement(ServerMeasurement newMeasurement){
        try {
            synchronized (lock) {
                measurementsList.add(newMeasurement);
            }

            //System.out.println("New measurement successfully stored.");
            return Constants.STATUS_SUCCESS;
        } catch (Exception e) {
            System.err.println("[ERROR] Couldn't insert a new measurement: " + e.getMessage());
            return Constants.ERR_UNKNOWN;
        }
    }
}
