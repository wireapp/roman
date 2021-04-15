package com.wire.bots.roman.integrations;

import com.wire.bots.roman.Application;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.Tools;
import com.wire.bots.roman.model.Attachment;
import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.Text;
import com.wire.lithium.models.NewBotResponseModel;
import com.wire.xenon.backend.models.Conversation;
import com.wire.xenon.backend.models.NewBot;
import com.wire.xenon.backend.models.User;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class IncomingMessageTest {
    private static final String BOT_CLIENT_DUMMY = "bot_client_dummy";
    private static final DropwizardTestSupport<Config> SUPPORT = new DropwizardTestSupport<>(
            Application.class, "roman.yaml",
            ConfigOverride.config("key", "TcZA2Kq4GaOcIbQuOvasrw34321cZAfLW4Ga54fsds43hUuOdcdm42"),
            ConfigOverride.config("apiHost", "http://localhost:8090"));
    private Client client;
    private Jdbi jdbi;

    @Before
    public void beforeClass() throws Exception {
        SUPPORT.before();
        Application app = SUPPORT.getApplication();
        client = app.getClient();
        jdbi = app.getJdbi();
    }

    @After
    public void afterClass() {
        SUPPORT.after();
    }

    @Test
    public void incomingMessageFromUserTest() {
        final UUID botId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID convId = UUID.randomUUID();
        final UUID providerId = UUID.randomUUID();
        final String serviceAuth = Tools.generateToken(botId);

        final String email = String.format("%s@email.com", serviceAuth);

        // Create some fake provider and service
        ProvidersDAO providersDAO = jdbi.onDemand(ProvidersDAO.class);
        providersDAO.insert("Test Provider", providerId, email, "hash", "password");
        providersDAO.update(providerId, "http://localhost:8080/messages", serviceAuth, UUID.randomUUID(), "Test Service");

        // Test Bot added into conv. BE calls POST /bots with NewBot object
        NewBotResponseModel newBotResponseModel = newBotFromBE(botId, userId, convId, serviceAuth);
        assertThat(newBotResponseModel.lastPreKey).isNotNull();
        assertThat(newBotResponseModel.preKeys).isNotNull();

        IncomingMessage txt = new IncomingMessage();
        txt.type = "text";
        txt.text = new Text();
        txt.text.data = "Hello Alice";

        Response res = post(serviceAuth, txt);

        assertThat(res.getStatus()).isEqualTo(200);

        IncomingMessage incomingMessage = new IncomingMessage();
        incomingMessage.type = "attachment";
        incomingMessage.attachment = new Attachment();
        byte[] pic = new byte[5 * 1024 * 1024];
        new Random().nextBytes(pic);
        incomingMessage.attachment.data = Base64.getEncoder().encodeToString(pic);
        incomingMessage.attachment.mimeType = "attachment/x";
        incomingMessage.attachment.filename = "test.x";

        res = post(serviceAuth, incomingMessage);
        assertThat(res.getStatus()).isEqualTo(200);

    }

    private Response post(String serviceAuth, IncomingMessage txt) {
        return client
                .target("http://localhost:" + SUPPORT.getLocalPort())
                .path("conversation")
                .request()
                .header("Authorization", "Bearer " + serviceAuth)
                .post(Entity.entity(txt, MediaType.APPLICATION_JSON_TYPE));
    }

    private NewBotResponseModel newBotFromBE(UUID botId, UUID userId, UUID convId, String serviceAuth) {
        NewBot newBot = new NewBot();
        newBot.id = botId;
        newBot.locale = "en";
        newBot.token = "token_dummy";
        newBot.client = BOT_CLIENT_DUMMY;
        newBot.origin = new User();
        newBot.origin.id = userId;
        newBot.origin.name = "user_name";
        newBot.origin.handle = "user_handle";
        newBot.conversation = new Conversation();
        newBot.conversation.id = convId;
        newBot.conversation.name = "conv_name";
        newBot.conversation.creator = userId;
        newBot.conversation.members = new ArrayList<>();

        Response res = client
                .target("http://localhost:" + SUPPORT.getLocalPort())
                .path("bots")
                .request()
                .header("Authorization", "Bearer " + serviceAuth)
                .post(Entity.entity(newBot, MediaType.APPLICATION_JSON_TYPE));

        assertThat(res.getStatus()).isEqualTo(201);

        return res.readEntity(NewBotResponseModel.class);
    }
}
