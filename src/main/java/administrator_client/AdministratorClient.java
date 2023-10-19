package administrator_client;

import administrator_client.threads.InputThread;
import com.sun.jersey.api.client.Client;
import shared.constants.Constants;
import shared.utils.LamportTimestamp;

import java.io.IOException;

public class AdministratorClient {

    static LamportTimestamp timestamp;

    public static void main(String[] args) throws IOException {
        // Lamport clock
        timestamp = new LamportTimestamp();

        Client client = Client.create();
        String serverAddress = "http://" + Constants.SERVER_ADDR + ":" + Constants.SERVER_PORT;

        InputThread inputThread = new InputThread(client, serverAddress);
        inputThread.start();
    }

}