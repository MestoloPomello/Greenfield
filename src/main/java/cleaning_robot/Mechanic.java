package cleaning_robot;

import java.util.concurrent.TimeUnit;

public class Mechanic {
    boolean hasToken = false;

    // da controllare tutto
    synchronized void fixRobot() {
        if (!hasToken) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            notify();
            hasToken = true;
        }
    }
}
