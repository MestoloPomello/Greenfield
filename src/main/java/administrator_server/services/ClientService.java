package administrator_server.services;

import administrator_server.beans.CleaningRobots;
import administrator_server.beans.Measurements;
import io.grpc.Server;
import shared.beans.AdaptedServerMeasurement;
import shared.beans.MeasurementsListResponse;
import shared.beans.RobotListResponse;
import shared.beans.ServerMeasurement;
import simulators.Measurement;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;

@Path("/client")
public class ClientService {

    // Lists the currently deployed cleaning robots
    @Path("/list_robots")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response listRobots() {
        System.out.println("[Admin Client] Received and satisfied LIST request.");
        RobotListResponse rls = new RobotListResponse(CleaningRobots.getInstance().getDeployedRobots());
        return Response.ok(rls).build();
    }

    // Lists the last n air pollution levels sent by a given robot
    @Path("/avg_robot/{robotId}/{n}")
    @GET
    @Produces({"application/json", "application/xml"})
    public Response avgRobot(@PathParam("robotId") int robotId, @PathParam("n") int n) {
        List<ServerMeasurement> sml = Measurements.getInstance().getMeasurementsList();
        sml.sort(Comparator.comparingLong(Measurement::getTimestamp));

        MeasurementsListResponse mlr;
        if (n > sml.size()) {
            mlr = new MeasurementsListResponse(sml);
        } else {
            mlr = new MeasurementsListResponse(sml.subList(0, n - 1));
        }

        System.out.println("[Admin Client] Received and satisfied AVG_ROBOT request.");

        return Response.ok(mlr).build();
    }
}
