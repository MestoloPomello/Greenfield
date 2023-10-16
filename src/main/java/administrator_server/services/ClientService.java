package administrator_server.services;

import administrator_server.beans.CleaningRobots;
import shared.beans.CleaningRobot;
import shared.constants.Constants;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/client")
public class ClientService {

    // Lists the currently deployed cleaning robots
    @Path("/list_robots")
    @GET
    public Response listRobots() {
        List<CleaningRobot> deployedRobots = CleaningRobots.getInstance().getDeployedRobots();
        return Response.ok(deployedRobots).build();
    }


}
