package administrator_server.beans;

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

    // TODO: save Measurements in persistent mode (i.e. SQL to be queried)

    @XmlElement(name="measurements_set")
    private List<ServerMeasurement> measurementsList;

    private static Measurements instance;

    private Measurements() { measurementsList = new ArrayList<ServerMeasurement>(); }

    // Singleton
    public synchronized static Measurements getInstance(){
        if (instance == null)
            instance = new Measurements();
        return instance;
    }

    public void setMeasurementsList(List<ServerMeasurement> measurementsList) {
        this.measurementsList = measurementsList;
    }

    public synchronized List<ServerMeasurement> getMeasurementsList() {
        return new ArrayList<>(measurementsList);
    }

    public synchronized int insertMeasurement(ServerMeasurement newMeasurement){
        try {
            measurementsList.add(newMeasurement);
            System.out.println("New measurement successfully stored.");
            return Constants.STATUS_SUCCESS;
        } catch (Exception e) {
            System.err.println("[ERROR] Couldn't insert a new measurement: " + e.getMessage());
            return Constants.ERR_UNKNOWN;
        }
    }
}
