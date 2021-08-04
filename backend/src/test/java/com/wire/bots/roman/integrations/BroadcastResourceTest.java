package com.wire.bots.roman.integrations;

import com.wire.bots.roman.Application;
import com.wire.bots.roman.Const;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.Tools;
import com.wire.bots.roman.model.*;
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

public class BroadcastResourceTest {
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
    public void broadcastTest() throws InterruptedException {
        final Random random = new Random();
        final UUID botId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID convId = UUID.randomUUID();
        final UUID providerId = UUID.randomUUID();
        final String serviceAuth = Tools.generateToken(providerId);

        final String email = String.format("%s@email.com", serviceAuth);

        // Create some fake provider and service
        ProvidersDAO providersDAO = jdbi.onDemand(ProvidersDAO.class);
        providersDAO.insert("Test Provider", providerId, email, "hash", "password");
        providersDAO.update(providerId, "http://localhost:8080/messages", serviceAuth, UUID.randomUUID(), "Test Service");

        // Test Bot added into conv. BE calls POST /bots with NewBot object
        NewBotResponseModel newBotResponseModel = newBotFromBE(botId, userId, convId, serviceAuth);
        assertThat(newBotResponseModel.lastPreKey).isNotNull();
        assertThat(newBotResponseModel.preKeys).isNotNull();

        IncomingMessage message = new IncomingMessage();
        message.type = "attachment";
        message.attachment = new Attachment();
        message.attachment.mimeType = "audio/x-m4a";
        message.attachment.name = "test.m4a";
        message.attachment.duration = 27000L;
        message.attachment.levels = new byte[100];
        message.attachment.size = 1024 * 1024 * 4L;
        random.nextBytes(message.attachment.levels);

        message.attachment.meta = new AssetMeta();
        message.attachment.meta.assetId = UUID.randomUUID().toString();
        message.attachment.meta.assetToken = UUID.randomUUID().toString();
        final byte[] sha256 = new byte[256];
        random.nextBytes(sha256);
        final byte[] otrKey = new byte[32];
        random.nextBytes(otrKey);

        message.attachment.meta.sha256 = Base64.getEncoder().encodeToString(sha256);
        message.attachment.meta.otrKey = Base64.getEncoder().encodeToString(otrKey);

        Response res = post(serviceAuth, message);
        assertThat(res.getStatus()).isEqualTo(200);

        Thread.sleep(5000);

        res = get(serviceAuth);
        assertThat(res.getStatus()).isEqualTo(200);

        final Report report = res.readEntity(Report.class);
        assertThat(report.broadcastId).isNotNull();
    }

    private Response post(String serviceAuth, IncomingMessage msg) {
        return client
                .target("http://localhost:" + SUPPORT.getLocalPort())
                .path("broadcast")
                .request()
                .header(Const.APP_KEY, serviceAuth)
                .post(Entity.entity(msg, MediaType.APPLICATION_JSON_TYPE));
    }

    private Response get(String serviceAuth) {
        return client
                .target("http://localhost:" + SUPPORT.getLocalPort())
                .path("broadcast")
                .request()
                .header(Const.APP_KEY, serviceAuth)
                .get();
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
