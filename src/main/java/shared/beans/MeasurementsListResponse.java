package shared.beans;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class MeasurementsListResponse {

    private final List<AdaptedServerMeasurement> adaptedMeasurementsList;

    public MeasurementsListResponse() {
        adaptedMeasurementsList = new ArrayList<>();
    };

    public MeasurementsListResponse(List<ServerMeasurement> measurementsList) {
        adaptedMeasurementsList = new ArrayList<>();
        for (ServerMeasurement sm : measurementsList) {
            adaptedMeasurementsList.add(new AdaptedServerMeasurement(sm));
        }
    }

    public List<ServerMeasurement> getMeasurementsList() {
        List<ServerMeasurement> measurementsList = new ArrayList<>();
        for (AdaptedServerMeasurement asm : adaptedMeasurementsList) {
            measurementsList.add(asm.getServerMeasurement());
        }
        return measurementsList;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (AdaptedServerMeasurement asm : adaptedMeasurementsList) {
            result.append(asm.getServerMeasurement().toString()).append("\n");
        }
        return result.toString();
    }
}
