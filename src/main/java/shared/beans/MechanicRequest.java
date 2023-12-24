package shared.beans;

public class MechanicRequest {
    private int robotId;
    private long timestamp;

    public MechanicRequest(int robotId, long timestamp) {
        this.robotId = robotId;
        this.timestamp = timestamp;
    }

    public int getRobotId() {
        return robotId;
    }

    public void setRobotId(int robotId) {
        this.robotId = robotId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Robot ID: " + robotId + " | Timestamp: " + timestamp;
    }
}