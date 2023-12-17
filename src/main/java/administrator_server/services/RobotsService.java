package administrator_server.services;

import shared.beans.CleaningRobot;
import administrator_server.beans.CleaningRobots;
import shared.beans.InputRobot;
import shared.beans.RobotCreationResponse;
import shared.constants.Constants;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/robot")
public class RobotsService {

    // Adds a new cleaning robot in the list of deployed robots
    @POST
    @Consumes({"application/json"})
    public Response insertRobot(InputRobot newRobot) {
        RobotCreationResponse addResult = CleaningRobots.getInstance().insertRobot(newRobot);

        switch(addResult.getStatus()) {
            case Constants.STATUS_SUCCESS:
                System.out.println("[Robot] Accepted insert request from robot with ID " +
                        newRobot.getId() + " and port " + newRobot.getPort());
                return Response.ok(addResult).build();
            case Constants.ERR_DUPED_ID:
                System.out.println("[Robot] Refused insert request from robot with duped ID " +
                        newRobot.getId() + " and port " + newRobot.getPort());
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            default:
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Updated an existing robot with a new configuration
    @Path("/{id}")
    @PUT
    @Consumes({"application/json"})
    public Response updateRobot(@PathParam("id") int robotId, CleaningRobot updatedRobot) {
        RobotCreationResponse updateResult = CleaningRobots.getInstance().updateRobot(robotId, updatedRobot);

        switch(updateResult.getStatus()) {
            case Constants.STATUS_SUCCESS:
                return Response.ok().build();
            case Constants.ERR_NO_ID:
                return Response.status(Response.Status.NOT_FOUND).build();
            default:
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Deletes an existing deployed robot
    @Path("/{id}")
    @DELETE
    public Response deleteRobot(@PathParam("id") int id) {
        int deleteResult = CleaningRobots.getInstance().deleteRobot(id);

        switch(deleteResult) {
            case Constants.STATUS_SUCCESS:
                return Response.ok().build();
            case Constants.ERR_NO_ID:
                return Response.status(Response.Status.NOT_FOUND).build();
            default:
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Deletes an existing deployed robot
    @Path("/crash/{id}")
    @DELETE
    public Response robotCrashed(@PathParam("id") int id) {
        CleaningRobots.getInstance().removeCrashedRobot(id);
        return Response.ok().build();
    }
}
