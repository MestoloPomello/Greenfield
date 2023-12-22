package shared.constants;

public final class Constants {

    // Success messages
    public static final int STATUS_SUCCESS = 0;

    // Error messages
    public static final int ERR_DUPED_ID = 1;
    public static final int ERR_UNKNOWN = 2;
    public static final int ERR_NO_ID = 3;

    // Server config
    public static final String SERVER_ADDR = "localhost";
    public static final int SERVER_PORT = 8080;

    // Broker config
    public static final String BROKER_ADDR = "tcp://localhost:1883";
    public static final String[] TOPICS = new String[] {
        "greenfield/pollution/district1",
        "greenfield/pollution/district2",
        "greenfield/pollution/district3",
        "greenfield/pollution/district4"
    };
    public static final int[] QOS = new int[] { 2, 2, 2, 2 };

    // Robot messages
    public static final String HELLO = "hello";
    public static final String QUIT = "quit";
    public static final String PING = "ping";
    public static final String PONG = "pong";
    public static final String FIX = "fix";

    // Mechanic messages
    public static final String NEED_MECHANIC = "need_mechanic";
    public static final String MECHANIC_OK = "ok";
    public static final String MECHANIC_NOT_OK = "not_ok";
    public static final String MECHANIC_RELEASE = "release";

}
