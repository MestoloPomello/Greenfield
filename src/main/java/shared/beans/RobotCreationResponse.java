package shared.beans;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class RobotCreationResponse extends CleaningRobot {

    private int status;
    private List<CleaningRobot> registeredRobots;

    public RobotCreationResponse() {
        super();
        registeredRobots = new ArrayList<CleaningRobot>();
    };

    public RobotCreationResponse(int id, int port, String address, int startingPosX, int startingPosY, int status, List<CleaningRobot> registeredRobots) {
        super(id, port, address, startingPosX, startingPosY);
        this.status = status;
        this.registeredRobots = registeredRobots;
    }

    public RobotCreationResponse(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<CleaningRobot> getRegisteredRobots() {
        return registeredRobots;
    }

    public void setRegisteredRobots(List<CleaningRobot> registeredRobots) {
        this.registeredRobots = registeredRobots;
    }
}
