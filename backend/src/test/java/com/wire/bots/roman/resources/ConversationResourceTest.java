package com.wire.bots.roman.resources;

import com.wire.bots.roman.Sender;
import com.wire.bots.roman.model.Attachment;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.PostMessageResult;
import com.wire.bots.roman.model.Text;
import com.wire.bots.roman.resources.dummies.AuthenticationFeatureDummy;
import com.wire.bots.roman.resources.dummies.Const;
import com.wire.xenon.backend.models.Conversation;
import io.dropwizard.testing.junit.ResourceTestRule;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ConversationResourceTest {
    private static final Sender sender = mock(Sender.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(AuthenticationFeatureDummy.class)
            .addResource(new ConversationResource(sender))
            .build();

    @Before
    public void setup() throws Exception {
        Conversation conversation = new Conversation();
        conversation.id = Const.CONV_ID;

        when(sender.getConversation(Const.BOT_ID)).thenReturn(conversation);
    }

    @After
    public void tearDown() {
        reset(sender);
    }

    @Test
    public void testPostTextIntoConversation() throws Exception {
        final IncomingMessage message = new IncomingMessage();
        message.type = "text";
        message.text = new Text();
        message.text.data = "Hi there!";

        when(sender.send(message, Const.BOT_ID)).thenReturn(Const.MSG_ID);

        final Response response = resources
                .target("conversation")
                .request()
                .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        final PostMessageResult result = response.readEntity(PostMessageResult.class);
        assertThat(result.messageId).isNotNull();
    }

    @Test
    public void testPostImageIntoConversation() {
        IncomingMessage message = new IncomingMessage();
        message.type = "attachment";
        message.attachment = new Attachment();
        message.attachment.mimeType = "image/jpeg";
        message.attachment.data = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aH" +
                "BwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zND";

        final Response response = resources
                .target("conversation")
                .request()
                .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        PostMessageResult result = response.readEntity(PostMessageResult.class);
        assertThat(result.messageId).isNotNull();
    }

    @Test
    public void testGetConversation() {
        final Conversation response = resources
                .target("conversation")
                .request()
                .get(Conversation.class);

        assertThat(response.id).isEqualTo(Const.CONV_ID);
    }
}
