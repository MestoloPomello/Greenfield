package administrator_server.beans;

import simulators.Measurement;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class ServerMeasurement extends Measurement implements Serializable {

    private int district;

    public ServerMeasurement(int district, String id, String type, double value, long timestamp) {
        super(id, type, value, timestamp);
        this.district = district;
    }

    public int getDistrict() {
        return district;
    }

    public void setDistrict(int district) {
        this.district = district;
    }

}
