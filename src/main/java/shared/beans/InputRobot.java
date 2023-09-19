package shared.beans;

public class InputRobot {

    private int id, port;
    private String address;

    public InputRobot() {};

    public InputRobot(int id, int port, String address) {
        this.id = id;
        this.port = port;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
