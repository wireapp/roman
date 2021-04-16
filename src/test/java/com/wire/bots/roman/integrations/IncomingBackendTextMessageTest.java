package com.wire.bots.roman.integrations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.waz.model.Messages;
import com.wire.bots.cryptobox.CryptoException;
import com.wire.bots.roman.Application;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.model.Config;
import com.wire.lithium.models.NewBotResponseModel;
import com.wire.xenon.backend.models.Conversation;
import com.wire.xenon.backend.models.NewBot;
import com.wire.xenon.backend.models.Payload;
import com.wire.xenon.backend.models.User;
import com.wire.xenon.crypto.CryptoFile;
import com.wire.xenon.models.otr.PreKeys;
import com.wire.xenon.models.otr.Recipients;
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
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class IncomingBackendTextMessageTest {
    private static final SecureRandom random = new SecureRandom();
    private static final String BOT_CLIENT_DUMMY = "bot_client_dummy";
    private static final String USER_CLIENT_DUMMY = "user_client_dummy";
    private static final DropwizardTestSupport<Config> SUPPORT = new DropwizardTestSupport<>(
            Application.class, "roman.yaml",
            ConfigOverride.config("key", "TcZA2Kq4GaOcIbQuOvasrw34321cZAfLW4Ga54fsds43hUuOdcdm42"),
            ConfigOverride.config("apiHost", "http://localhost:8090"));

    private final String serviceAuth = new BigInteger(64, random).toString(16);
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
    public void test() throws CryptoException, JsonProcessingException {
        final UUID botId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        final UUID convId = UUID.randomUUID();
        final UUID providerId = UUID.randomUUID();
        final String email = String.format("%s@email.com", serviceAuth);

        // Create some fake provider and service
        ProvidersDAO providersDAO = jdbi.onDemand(ProvidersDAO.class);
        providersDAO.insert("Test Provider", providerId, email, "hash", "password");
        providersDAO.update(providerId, "http://localhost:8080/messages", serviceAuth, UUID.randomUUID(), "Test Service");

        // Test Bot added into conv. BE calls POST /bots with NewBot object
        NewBotResponseModel newBotResponseModel = newBotFromBE(botId, userId, convId);
        assertThat(newBotResponseModel.lastPreKey).isNotNull();
        assertThat(newBotResponseModel.preKeys).isNotNull();

        CryptoFile crypto = new CryptoFile("data", botId);
        PreKeys preKeys = new PreKeys(newBotResponseModel.preKeys, USER_CLIENT_DUMMY, userId);

        // Test new Text message is sent to Roman by the BE. BE calls POST /bots/{botId}/messages with Payload obj
        Recipients recipients = crypto.encrypt(preKeys, generateTextMessage("Hello Bob"));
        String cypher = recipients.get(userId, USER_CLIENT_DUMMY);
        Response res = newOtrMessageFromBackend(botId, userId, cypher);
        assertThat(res.getStatus()).isEqualTo(200);

        crypto.close();
    }

    private NewBotResponseModel newBotFromBE(UUID botId, UUID userId, UUID convId) {
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

    private Response newOtrMessageFromBackend(UUID botId, UUID userId, String cypher) {
        Payload payload = new Payload();
        payload.type = "conversation.otr-message-add";
        payload.from = userId;
        payload.time = new Date().toString();
        payload.data = new Payload.Data();
        payload.data.sender = USER_CLIENT_DUMMY;
        payload.data.recipient = BOT_CLIENT_DUMMY;
        payload.data.text = cypher;

        return client
                .target("http://localhost:" + SUPPORT.getLocalPort())
                .path("bots")
                .path(botId.toString())
                .path("messages")
                .request()
                .header("Authorization", "Bearer " + serviceAuth)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON_TYPE));
    }

    @SuppressWarnings("SameParameterValue")
    private byte[] generateTextMessage(String content) {
        Messages.Text.Builder text = Messages.Text.newBuilder()
                .setContent(content)
                .setQuote(Messages.Quote.newBuilder().setQuotedMessageId(UUID.randomUUID().toString()));

        return Messages.GenericMessage.newBuilder()
                .setMessageId(UUID.randomUUID().toString())
                .setText(text)
                .build()
                .toByteArray();
    }
}
