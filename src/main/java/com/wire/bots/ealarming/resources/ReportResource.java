package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.model.Report;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api
@Path("/report/{alertId}")
@Produces(MediaType.APPLICATION_JSON)
public class ReportResource {
    private final AuthValidator validator;
    private final Alert2UserDAO alert2UserDAO;

    public ReportResource(Alert2UserDAO alert2UserDAO, AuthValidator validator) {
        this.alert2UserDAO = alert2UserDAO;
        this.validator = validator;
    }

    @GET
    @ApiOperation(value = "Get Report for this Alert")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Report")})
    public Response get(@ApiParam @PathParam("alertId") int alertId) {
        try {
            Report ret = new Report();
            List<Alert2UserDAO._Pair> pairs = alert2UserDAO.report(alertId);

            for (Alert2UserDAO._Pair pair : pairs) {
                switch (pair.type) {
                    case 0:
                        ret.unknown = pair.count;
                        break;
                    case 1:
                        ret.sent = pair.count;
                        break;
                    case 2:
                        ret.delivered = pair.count;
                        break;
                    case 3:
                        ret.read = pair.count;
                        break;
                    case 4:
                        ret.responded = pair.count;
                        break;
                }
            }
            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            Logger.error("ReportResource.get(%d): %s", alertId, e);
            return Response
                    .ok(e)
                    .status(500)
                    .build();
        }
    }
}