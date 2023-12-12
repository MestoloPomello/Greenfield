package shared.utils;

import static shared.utils.Utils.getRandomInt;


public class LamportTimestamp {

    private static int tsOffset;
    private static final Object tsLock = new Object();
    private int timestamp;

    public LamportTimestamp() {
        timestamp = 1;
        tsOffset = getRandomInt(1, 10);
    }

    public void setTimestamp(int newValue) {
        synchronized (tsLock) {
            timestamp = newValue;
        }
    }

    public void increaseTimestamp() {
        synchronized (tsLock) {
            timestamp += tsOffset;
        }
    }

    public int getTimestamp() {
        synchronized (tsLock) {
            return timestamp;
        }
    }

    public int compareAndIncreaseTimestamp(int receivedTimestamp) {
        if (receivedTimestamp > timestamp) {
            timestamp = receivedTimestamp;
        }
        return ++timestamp;
    }

}
