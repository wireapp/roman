package com.wire.bots.roman;

import com.wire.bots.cryptobox.CryptoException;
import com.wire.bots.roman.model.Attachment;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.Mention;
import com.wire.lithium.ClientRepo;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.*;
import com.wire.xenon.backend.models.Conversation;
import com.wire.xenon.models.AssetKey;
import com.wire.xenon.tools.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class Sender {

    private final ClientRepo repo;

    public Sender(ClientRepo repo) {
        this.repo = repo;
    }

    @Nullable
    public UUID send(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient client = repo.getClient(botId)) {
            if (client == null)
                return null;

            return send(message, client);
        }
    }

    private UUID send(IncomingMessage message, WireClient client) throws Exception {
        switch (message.type) {
            case "text": {
                MessageText text = new MessageText(message.text.data);
                if (message.text.mentions != null) {
                    for (Mention mention : message.text.mentions)
                        text.addMention(mention.userId, mention.offset, mention.length);
                }
                client.send(text);
                return text.getMessageId();
            }
            case "attachment": {
                if (message.attachment.mimeType.startsWith("image")) {
                    final byte[] decode = Base64.getDecoder().decode(message.attachment.data);
                    final Picture picture = new Picture(decode, message.attachment.mimeType);
                    final AssetKey assetKey = client.uploadAsset(picture);
                    picture.setAssetToken(assetKey.token);
                    picture.setAssetKey(assetKey.key);
                    client.send(picture);
                    return picture.getMessageId();
                }

                return sendAttachment(message, client);
            }
            case "poll": {
                if (message.poll.type.equals("create")) {
                    return sendNewPoll(message, client);
                }
                if (message.poll.type.equals("confirmation")) {
                    return sendPollConfirmation(message, client);
                }
            }
            break;
        }

        return null;
    }

    private UUID sendNewPoll(IncomingMessage message, WireClient client) throws Exception {
        MessageText messageText = new MessageText(message.text.data);
        if (message.text.mentions != null) {
            for (Mention mention : message.text.mentions)
                messageText.addMention(mention.userId, mention.offset, mention.length);
        }

        Poll poll = new Poll(message.poll.id);
        poll.addText(messageText);

        int i = 0;
        for (String caption : message.poll.buttons) {
            poll.addButton("" + i++, caption);
        }

        Logger.info("poll.create: id: %s", message.poll.id);

        client.send(poll);

        return poll.getMessageId();
    }

    private UUID sendAttachment(IncomingMessage message, WireClient client) throws Exception {
        UUID messageId = UUID.randomUUID();

        final Attachment attachment = message.attachment;
        final byte[] decode = Base64.getDecoder().decode(attachment.data);

        FileAssetPreview preview = new FileAssetPreview(attachment.filename,
                attachment.mimeType,
                decode.length,
                messageId);
        FileAsset asset = new FileAsset(decode, attachment.mimeType, messageId);

        client.send(preview);
        final AssetKey assetKey = client.uploadAsset(asset);
        asset.setAssetKey(assetKey.key);
        asset.setAssetToken(assetKey.token);
        client.send(asset);
        return messageId;
    }

    private UUID sendPollConfirmation(IncomingMessage message, WireClient client) throws Exception {
        ButtonActionConfirmation confirmation = new ButtonActionConfirmation(
                message.poll.id,
                message.poll.offset.toString());

        Logger.info("poll.confirmation: pollId: %s, offset: %s", message.poll.id, message.poll.offset);

        client.send(confirmation, message.poll.userId);

        return confirmation.getMessageId();
    }

    public Conversation getConversation(UUID botId) throws IOException, CryptoException {
        try (WireClient client = repo.getClient(botId)) {
            return client.getConversation();
        }
    }
}
