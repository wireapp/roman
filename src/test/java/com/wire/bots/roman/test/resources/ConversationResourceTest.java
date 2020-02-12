package com.wire.bots.roman.test.resources;

import com.wire.bots.cryptobox.CryptoException;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.resources.ConversationResource;
import com.wire.bots.roman.test.resources.dummies.AuthenticationFeatureDummy;
import com.wire.bots.roman.test.resources.dummies.WireClientDummy;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.server.model.Conversation;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static com.wire.bots.roman.test.resources.dummies.Const.BOT_ID;
import static com.wire.bots.roman.test.resources.dummies.Const.CONV_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ConversationResourceTest {
    private static final ClientRepo clientRepo = mock(ClientRepo.class);
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(AuthenticationFeatureDummy.class)
            .addResource(new ConversationResource(clientRepo))
            .build();
    private static final WireClientDummy botClient = new WireClientDummy();

    @Before
    public void setup() throws IOException, CryptoException {
        when(clientRepo.getClient(BOT_ID)).thenReturn(botClient);
    }

    @After
    public void tearDown() {
        reset(clientRepo);
    }

    @Test
    public void testPostIntoConversation() {
        IncomingMessage message = new IncomingMessage();
        message.type = "text";
        message.text = "Hi there!";

        final Response response = resources
                .target("conversation")
                .request()
                .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testGetConversation() {
        final Conversation response = resources
                .target("conversation")
                .path(CONV_ID.toString())
                .request()
                .get(Conversation.class);

        assertThat(response.id).isEqualTo(CONV_ID);
    }
}
