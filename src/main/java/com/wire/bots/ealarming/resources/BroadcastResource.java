package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.DAO.AlertDAO;
import com.wire.bots.ealarming.DAO.GroupsDAO;
import com.wire.bots.ealarming.DAO.User2BotDAO;
import com.wire.bots.ealarming.model.Alert;
import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.ealarming.model.Group;
import com.wire.bots.ealarming.model.User;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Api
@Path("/broadcast")
@Produces(MediaType.APPLICATION_JSON)
public class BroadcastResource {
    private final AlertDAO alertDAO;
    private final Alert2UserDAO alert2UserDAO;
    private final GroupsDAO groupsDAO;
    private final User2BotDAO user2BotDAO;
    private final ClientRepo clientRepo;
    private final AuthValidator validator;

    public BroadcastResource(AlertDAO alertDAO,
                             Alert2UserDAO alert2UserDAO,
                             GroupsDAO groupsDAO,
                             User2BotDAO user2BotDAO,
                             ClientRepo clientRepo,
                             AuthValidator validator) {
        this.alertDAO = alertDAO;
        this.alert2UserDAO = alert2UserDAO;
        this.groupsDAO = groupsDAO;
        this.user2BotDAO = user2BotDAO;
        this.clientRepo = clientRepo;
        this.validator = validator;
    }

    @POST
    @Path("{alertId}")
    @ApiOperation(value = "Broadcast Alert")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Report")})
    public Response post(@ApiParam @PathParam("alertId") int alertId) {
        try {
            _Result result = new _Result();

            Alert alert = alertDAO.get(alertId);
            List<Alert2User> users = alert2UserDAO.selectUsers(alertId);
            List<Group> groups = alertDAO.selectGroups(alertId);

            for (Group group : groups) {
                List<User> groupUsers = groupsDAO.selectUsers(group.id);
                for (User user : groupUsers) {
                    UUID userId = user.userId;
                    UUID botId = user2BotDAO.get(userId);
                    if (botId != null) {
                        try (WireClient client = clientRepo.getClient(botId)) {
                            UUID messageId = client.sendText(alert.message);
                            alert2UserDAO.insertStatus(alertId, userId, messageId, 1);
                            result.sent++;
                        } catch (Exception e) {

                        }
                    }
                }
            }

            for (Alert2User user : users) {
                UUID userId = user.userId;
                UUID botId = user2BotDAO.get(userId);
                if (botId != null) {
                    try (WireClient client = clientRepo.getClient(botId)) {
                        UUID messageId = client.sendText(alert.message);
                        alert2UserDAO.insertStatus(alertId, userId, messageId, 1);
                        result.sent++;
                    } catch (Exception e) {

                    }
                }
            }

            return Response.
                    ok(result).
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.post: %s", e);
            return Response
                    .ok(e)
                    .status(500)
                    .build();
        }
    }

    class _Result {
        public int sent = 0;
    }
}