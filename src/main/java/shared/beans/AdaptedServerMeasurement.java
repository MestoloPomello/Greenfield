package shared.beans;

import java.io.Serializable;

public class AdaptedServerMeasurement implements Serializable {

    private final double value;
    private final int district;
    private final String id;
    private final long timestamp;
    private final String type;

    public AdaptedServerMeasurement() {
        value = -1;
        district = -1;
        id = "null";
        timestamp = -1;
        type = "null";
    }

    public AdaptedServerMeasurement(ServerMeasurement sm) {
        value = sm.getValue();
        district = sm.getDistrict();
        id = sm.getId();
        timestamp = sm.getTimestamp();
        type = sm.getType();
    }

    public ServerMeasurement getServerMeasurement() {
        return new ServerMeasurement(
                district,
                id,
                type,
                value,
                timestamp
        );
    }

}