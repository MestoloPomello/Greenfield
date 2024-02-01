package shared.beans;

public class MechanicRequest {
    private int robotId;
    private int timestamp;

    public MechanicRequest(int robotId, int timestamp) {
        this.robotId = robotId;
        this.timestamp = timestamp;
    }

    public int getRobotId() {
        return robotId;
    }

    public void setRobotId(int robotId) {
        this.robotId = robotId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Robot ID: " + robotId + " | Timestamp: " + timestamp;
    }
}