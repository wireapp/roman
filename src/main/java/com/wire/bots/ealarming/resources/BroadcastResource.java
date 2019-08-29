package com.wire.bots.ealarming.resources;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wire.bots.ealarming.DAO.*;
import com.wire.bots.ealarming.model.Alert;
import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.ealarming.model.Group;
import com.wire.bots.ealarming.model.User;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.wire.bots.ealarming.Tools.validateToken;

@Api
@Path("/broadcast")
@Produces(MediaType.APPLICATION_JSON)
public class BroadcastResource {
    private static final MustacheFactory mf = new DefaultMustacheFactory();

    private final AlertDAO alertDAO;
    private final Alert2UserDAO alert2UserDAO;
    private final GroupsDAO groupsDAO;
    private final User2BotDAO user2BotDAO;
    private final ClientRepo clientRepo;
    private final AttachmentDAO attachmentDAO;

    public BroadcastResource(DBI jdbi, ClientRepo clientRepo) {
        this.alertDAO = jdbi.onDemand(AlertDAO.class);
        this.alert2UserDAO = jdbi.onDemand(Alert2UserDAO.class);
        this.groupsDAO = jdbi.onDemand(GroupsDAO.class);
        this.user2BotDAO = jdbi.onDemand(User2BotDAO.class);
        this.attachmentDAO = jdbi.onDemand(AttachmentDAO.class);

        this.clientRepo = clientRepo;
    }

    @POST
    @Path("{alertId}")
    @ApiOperation(value = "Broadcast an Alert", response = _Result.class)
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated"),
            @ApiResponse(code = 404, message = "Unknown Alert ID")})
    public Response post(@ApiParam(hidden = true) @CookieParam("Authorization") String token,
                         @ApiParam @PathParam("alertId") int alertId) {
        try {
            String subject = validateToken(token);

            Alert alert = alertDAO.get(alertId);

            if (alert == null) {
                return Response.
                        status(404).
                        build();
            }

            alert.responses = alertDAO.selectResponses(alertId);

            HashSet<_Task> users = extractUsers(alertId);

            _Result result = new _Result();
            result.sent = sendAlert(alert, users);

            return Response.
                    ok(result).
                    build();
        } catch (SignatureException e) {
            Logger.warning("BroadcastResource.post(%d) %s", alertId, e);
            return Response.
                    ok(new ErrorMessage("Not authenticated")).
                    status(403).
                    build();
        } catch (Exception e) {
            Logger.error("BroadcastResource.post(%d): %s", alertId, e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private int sendAlert(Alert alert, HashSet<_Task> tasks) throws IOException {
        _Message message = new _Message();
        message.title = alert.title;
        message.body = alert.message;
        message.severity = severity(alert.severity);
        message.contact = alert.contact;
        message.responses = alert.responses;

        String text = execute(message);
        int sent = 0;
        for (_Task task : tasks) {
            try (WireClient client = clientRepo.getClient(task.botId)) {
                UUID messageId = client.sendText(text);
                Alert2User.Type status = Alert2User.Type.SENT;
                int insert = alert2UserDAO.insertStatus(alert.id, task.userId, status.ordinal(), messageId, null);
                if (insert == 0)
                    Logger.warning("sendAlert: alert: %d, user: %s, msgId: %s, %s. insert: %s",
                            alert.id, task.userId, messageId, status, insert);
                sent++;
            } catch (Exception ignore) {

            }
        }
        return sent;
    }

    private String severity(Integer severity) {
        switch (severity) {
            case 1:
                return "ℹ️";
            case 2:
                return "❗";
            case 3:
                return "🔥";
            default:
                return "✅";
        }
    }

    private HashSet<_Task> extractUsers(int alertId) {
        HashSet<_Task> tasks = new HashSet<>();
        List<Alert2User> users = alert2UserDAO.listUsers(alertId);
        for (Alert2User user : users) {
            UUID userId = user.userId;
            UUID botId = user2BotDAO.get(userId);
            if (botId != null) {
                _Task task = new _Task();
                task.botId = botId;
                task.userId = userId;
                tasks.add(task);
            }
        }
        return tasks;
    }

    private HashSet<_Task> extractGroups(int alertId) {
        HashSet<_Task> tasks = new HashSet<>();
        List<Group> groups = alertDAO.selectGroups(alertId);
        for (Group group : groups) {
            List<User> groupUsers = groupsDAO.selectUsers(group.id);
            for (User user : groupUsers) {
                UUID userId = user.userId;
                UUID botId = user2BotDAO.get(userId);
                if (botId != null) {
                    _Task task = new _Task();
                    task.botId = botId;
                    task.userId = userId;
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    private String execute(_Message message) throws IOException {
        try (StringWriter sw = new StringWriter()) {
            Mustache mustache = compileTemplate();
            mustache.execute(new PrintWriter(sw), message).flush();
            return sw.toString();
        }
    }

    private Mustache compileTemplate() {
        String path = "templates/message.txt";
        return mf.compile(path);
    }

    static class _Result {
        public int sent = 0;
    }

    class _Message {
        String title;
        String body;
        UUID contact;
        String severity;
        List<String> responses;
    }

    class _Task {
        UUID userId;
        UUID botId;
    }
}