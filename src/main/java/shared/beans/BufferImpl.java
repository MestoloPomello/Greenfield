package shared.beans;

import simulators.Buffer;
import simulators.Measurement;

import java.util.ArrayList;
import java.util.List;

public class BufferImpl implements Buffer {
    final List<Measurement> measurementsBuffer = new ArrayList<>();
    final List<Measurement> averages = new ArrayList<>();

    @Override
    public synchronized void addMeasurement(Measurement m) {
        // 8 = full buffer
        measurementsBuffer.add(m);
        if (measurementsBuffer.size() == 8) {
            // Compute the average and add it to the list
            double sum = 0.0;
            for (Measurement measurement : measurementsBuffer) {
                sum += measurement.getValue();
            }

            averages.add(new Measurement(
                    "none",
                    "none",
                    sum / 8,
                    0
            ));

            // Slide the window
            measurementsBuffer.subList(0, 4).clear();
        }
    }

    @Override
    public synchronized List<Measurement> readAllAndClean() {
        // Create a copy of the averages' array, clear the original and return the copy
        List<Measurement> averagesCopy = new ArrayList<>(averages);
        averages.clear();
        return averagesCopy;
    }
}
