package shared.utils;

import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static int getRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static int[] generateCoordinatesForDistrict(int district) {
        switch (district) {
            case 1:
                return new int[]{getRandomInt(0, 4), getRandomInt(0, 4)};
            case 2:
                return new int[]{getRandomInt(0, 4), getRandomInt(5, 9)};
            case 3:
                return new int[]{getRandomInt(5, 9), getRandomInt(5, 9)};
            case 4:
                return new int[]{getRandomInt(5, 9), getRandomInt(0, 4)};
            default:
                return new int[]{0, 0};
        }
    }
}
