package com.wire.bots.roman.integrations;

import com.wire.bots.roman.Application;
import com.wire.bots.roman.model.Config;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.NewBotResponseModel;
import com.wire.bots.sdk.server.model.User;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class NewBotTest {
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
    public void testNewBot() {
        NewBot newBot = new NewBot();
        newBot.id = UUID.randomUUID();
        newBot.locale = "en";
        newBot.token = "token_dummy";
        newBot.origin = new User();
        newBot.origin.id = UUID.randomUUID();
        newBot.origin.name = "user_name_dummy";
        newBot.origin.handle = "user_handle_dummy";
        newBot.client = "client_dummy";
        newBot.conversation = new Conversation();
        newBot.conversation.id = UUID.randomUUID();
        newBot.conversation.name = "conv_name_dummy";
        newBot.conversation.creator = UUID.randomUUID();
        newBot.conversation.members = new ArrayList<>();

        String serviceAuth = "m7MRW984gHHVFmkWaR5yyIdH";

        Application app = SUPPORT.getApplication();

        Response res = app.getClient()
                .target("http://localhost:" + SUPPORT.getLocalPort())
                .path("proxy")
                .path("bots")
                .request()
                .header("Authorization", "Bearer " + serviceAuth)
                .post(Entity.entity(newBot, MediaType.APPLICATION_JSON_TYPE));

        assertThat(res.getStatus()).isEqualTo(201);

        NewBotResponseModel newBotResponseModel = res.readEntity(NewBotResponseModel.class);

        assertThat(newBotResponseModel.lastPreKey).isNotNull();
        assertThat(newBotResponseModel.preKeys).isNotNull();

    }
}