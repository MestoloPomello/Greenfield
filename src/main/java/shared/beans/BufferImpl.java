package shared.beans;

import simulators.Buffer;
import simulators.Measurement;

import java.util.ArrayList;
import java.util.List;

public class BufferImpl implements Buffer {
    final List<Measurement> measurementsBuffer = new ArrayList<>();

    @Override
    public synchronized void addMeasurement(Measurement m) {
        // 8 = full buffer
        if (measurementsBuffer.size() == 8) {
            measurementsBuffer.remove(0);
        }
        measurementsBuffer.add(m);
    }

    @Override
    public synchronized List<Measurement> readAllAndClean() {
        List<Measurement> listCopy = new ArrayList<>(measurementsBuffer);
        measurementsBuffer.clear();
        return listCopy;
    }
}
