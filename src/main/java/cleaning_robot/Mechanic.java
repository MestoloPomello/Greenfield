package cleaning_robot;

import administrator_server.beans.Measurements;
import shared.beans.ServerMeasurement;

import java.util.ArrayList;

public class Mechanic {
    //boolean hasToken = false;

    private final static Object lock = new Object();
    private static Mechanic instance;

    private Mechanic() { }

    // Singleton
    public static Mechanic getInstance(){
        synchronized (lock) {
            if (instance == null)
                instance = new Mechanic();
            return instance;
        }
    }

    // da controllare tutto
//    synchronized void fixRobot() {
//        if (!hasToken) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        } else {
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//            notify();
//            hasToken = true;
//        }
//    }
}
