package administrator_server.services;

import administrator_server.beans.CleaningRobots;
import shared.beans.RobotListResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/client")
public class ClientService {

    // Lists the currently deployed cleaning robots
    @Path("/list_robots")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response listRobots() {
        System.out.println("[Admin Client] Received and satisfied LIST request.");
        RobotListResponse rls = CleaningRobots.getInstance().getDeployedRobots();
        return Response.ok(rls).build();
    }

    // Lists the last n air pollution levels sent by a given robot
    @Path("/avg_robot/{robotId}/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response avgRobot(@PathParam("robotId") int robotId, @PathParam("n") int n) {
        System.out.println("[Admin Client] Received and satisfied AVG_ROBOT request.");

        // DA SISTEMARE

        return Response.ok(rls).build();
    }
}
