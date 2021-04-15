package com.wire.bots.roman;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper mapper = new ObjectMapper();

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

    private static byte[] base64Decode(IncomingMessage message) {
        return Base64.getDecoder().decode(message.attachment.data);
    }

    private UUID send(IncomingMessage message, WireClient client) throws Exception {
        switch (message.type) {
            case "text": {
                return sendText(message, client);
            }
            case "attachment": {
                if (message.attachment.mimeType.startsWith("image")) {
                    return sendPicture(message, client);
                }
                if (message.attachment.mimeType.startsWith("audio")) {
                    return sendAudio(message, client);
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
                break;
            }
            case "call": {
                return sendCall(message, client);
            }
        }

        return null;
    }

    private UUID sendCall(IncomingMessage message, WireClient client) throws Exception {
        String content = message.call != null
                ? mapper.writeValueAsString(message.call)
                : "{\"version\":\"3.0\",\"type\":\"GROUPSTART\",\"sessid\":\"\",\"resp\":false}";
        final Calling calling = new Calling(content);
        client.send(calling);
        return calling.getMessageId();
    }

    private UUID sendText(IncomingMessage message, WireClient client) throws Exception {
        MessageText text = new MessageText(message.text.data);
        text.setExpectsReadConfirmation(true);
        if (message.text.mentions != null) {
            for (Mention mention : message.text.mentions)
                text.addMention(mention.userId, mention.offset, mention.length);
        }
        client.send(text);
        return text.getMessageId();
    }

    private UUID sendAudio(IncomingMessage message, WireClient client) throws Exception {
        final byte[] bytes = base64Decode(message);

        final AudioPreview preview = new AudioPreview(bytes,
                message.attachment.filename,
                message.attachment.mimeType,
                message.attachment.duration);
        final AudioAsset audioAsset = new AudioAsset(bytes, preview);
        final AssetKey assetKey = client.uploadAsset(audioAsset);
        audioAsset.setAssetToken(assetKey.token);
        audioAsset.setAssetKey(assetKey.key);
        client.send(audioAsset);
        return audioAsset.getMessageId();
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
        final byte[] decode = base64Decode(message);

        FileAssetPreview preview = new FileAssetPreview(attachment.filename,
                attachment.mimeType,
                decode.length,
                messageId);
        FileAsset asset = new FileAsset(decode, attachment.mimeType, messageId);

        client.send(preview);
        final AssetKey assetKey = client.uploadAsset(asset);
        asset.setAssetKey(assetKey.key != null ? assetKey.key : "");
        asset.setAssetToken(assetKey.token != null ? assetKey.token : "");
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

    private UUID sendPicture(IncomingMessage message, WireClient client) throws Exception {
        final Picture picture = new Picture(base64Decode(message), message.attachment.mimeType);
        final AssetKey assetKey = client.uploadAsset(picture);
        picture.setAssetToken(assetKey.token);
        picture.setAssetKey(assetKey.key);
        client.send(picture);
        return picture.getMessageId();
    }
}
