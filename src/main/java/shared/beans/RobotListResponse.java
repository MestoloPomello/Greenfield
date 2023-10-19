package shared.beans;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class RobotListResponse {

    private List<CleaningRobot> deployedRobots;

    public RobotListResponse() {
        deployedRobots = new ArrayList<>();
    }

    public RobotListResponse(List<CleaningRobot> deployedRobots) {
        this.deployedRobots = deployedRobots;
    };

    public List<CleaningRobot> getDeployedRobots() {
        if (deployedRobots.isEmpty())
            return new ArrayList<>();
        else return deployedRobots;
    }

    public void setDeployedRobots(List<CleaningRobot> deployedRobots) {
        this.deployedRobots = deployedRobots;
    }
}
