package shared.beans;

import simulators.Measurement;

import java.util.ArrayList;
import java.util.List;

public class MeasurementsListResponse {

    private List<Measurement> measurementsList;

    public MeasurementsListResponse() {
        measurementsList = new ArrayList<>();
    };

    public MeasurementsListResponse(List<Measurement> measurementsList) {
        this.measurementsList = measurementsList;
    }

    public List<Measurement> getMeasurementsList() {
        return measurementsList;
    }

    public void setMeasurementsList(List<Measurement> measurementsList) {
        this.measurementsList = measurementsList;
    }
}
