package shared.beans;

import simulators.Measurement;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

public class ServerMeasurement extends Measurement implements Serializable {

    private int district, robotId;

    /*public ServerMeasurement() {
        super("null", "null", -1, -1);
        district = -1;
    }*/

    public ServerMeasurement(int district, int robotId, String id, String type, double value, long timestamp) {
        super(id, type, value, timestamp);
        this.district = district;
        this.robotId = robotId;
    }

    public int getDistrict() {
        return district;
    }

    public void setDistrict(int district) {
        this.district = district;
    }

    public int getRobotId() {
        return robotId;
    }

    public void setRobotId(int robotId) {
        this.robotId = robotId;
    }
}
