package com.wire.bots.roman.resources;

import com.wire.bots.cryptobox.CryptoException;
import com.wire.bots.roman.Sender;
import com.wire.bots.roman.model.Attachment;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.PostMessageResult;
import com.wire.bots.roman.model.Text;
import com.wire.bots.roman.resources.dummies.Const;
import com.wire.xenon.backend.models.Conversation;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static com.wire.bots.roman.resources.dummies.Const.CONV_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ConversationResourceTest {
    private static final Sender sender = mock(Sender.class);
    public static final ResourceExtension resources = ResourceExtension.builder()
            .addResource(new ConversationResource(sender))
            .build();

    private final Conversation conversation = new Conversation() {{
        id = CONV_ID;
    }};

    @AfterEach
    public void tearDown() {
        reset(sender);
    }

    @Test
    public void testPostTextIntoConversation() throws Exception {
        final IncomingMessage message = new IncomingMessage() {{
            this.type = "text";
            this.text = new Text();
            this.text.data = "Hi there!";
        }};
        when(sender.send(any(), any())).thenReturn(Const.MSG_ID);

        PostMessageResult result;
        try (Response response = resources
                .target("conversation")
                .request()
                .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE))) {

            assertThat(response.getStatus()).isEqualTo(200);
            result = response.readEntity(PostMessageResult.class);
        }
        assertThat(result.messageId).isNotNull();
    }

    @Test
    public void testPostImageIntoConversation() throws Exception {
        when(sender.send(any(), any())).thenReturn(Const.MSG_ID);

        IncomingMessage message = new IncomingMessage();
        message.type = "attachment";
        message.attachment = new Attachment();
        message.attachment.mimeType = "image/jpeg";
        message.attachment.data = "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aH" +
                "BwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zND";

        final Response response = resources
                .target("/conversation")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        PostMessageResult result = response.readEntity(PostMessageResult.class);
        assertThat(result.messageId).isNotNull();
    }

    @Test
    public void testGetConversation() throws IOException, CryptoException {
        when(sender.getConversation(any())).thenReturn(conversation);
        final Conversation response = resources
                .target("/conversation")
                .request()
                .get(Conversation.class);

        assertThat(response).isNotNull();
        assertThat(response.id).isEqualTo(CONV_ID);
    }
}
