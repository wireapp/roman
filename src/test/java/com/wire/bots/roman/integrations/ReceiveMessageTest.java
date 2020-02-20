package com.wire.bots.roman.integrations;

import com.wire.bots.roman.Application;
import com.wire.bots.roman.model.Config;
import com.wire.bots.sdk.server.model.Payload;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ReceiveMessageTest {
    private static final DropwizardTestSupport<Config> SUPPORT = new DropwizardTestSupport<>(
            Application.class, "roman.yaml",
            ConfigOverride.config("key", "TcZA2Kq4GaOcIbQuOvasrw34321cZAfLW4Ga54fsds43hUuOdcdm42"));

    @Before
    public void beforeClass() {
        SUPPORT.before();
    }

    @After
    public void afterClass() {
        SUPPORT.after();
    }

    @Test
    public void testIncomingMessageFromBE() {
        UUID botId = UUID.randomUUID();

        Payload payload = new Payload();
        payload.type = "conversation.otr-message-add";
        payload.from = UUID.randomUUID();
        payload.time = new Date().toString();
        payload.data = new Payload.Data();
        payload.data.sender = "sender";
        payload.data.recipient = "client_dummy";

        String serviceAuth = "m7MRW984gHHVFmkWaR5yyIdH";

        Application app = SUPPORT.getApplication();

        Response res = app.getClient()
                .target("http://localhost:" + SUPPORT.getLocalPort())
                .path("proxy")
                .path("bots")
                .path(botId.toString())
                .path("messages")
                .request()
                .header("Authorization", "Bearer " + serviceAuth)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON_TYPE));

        assertThat(res.getStatus()).isEqualTo(200);

    }
}