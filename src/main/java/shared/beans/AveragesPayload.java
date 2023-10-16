package shared.beans;

import simulators.Measurement;

import java.util.List;

public class AveragesPayload {
    private List<Measurement> averages;
    private int robotID;
    private int timestamp;

    public AveragesPayload(List<Measurement> averages, int robotID, int timestamp) {
        this.averages = averages;
        this.robotID = robotID;
        this.timestamp = timestamp;
    }

    public List<Measurement> getAverages() {
        return averages;
    }

    public void setAverages(List<Measurement> averages) {
        this.averages = averages;
    }

    public int getRobotID() {
        return robotID;
    }

    public void setRobotID(int robotID) {
        this.robotID = robotID;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
