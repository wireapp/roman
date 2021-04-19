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

    private static byte[] base64Decode(IncomingMessage message) {
        return Base64.getDecoder().decode(message.attachment.data);
    }

    @Nullable
    public UUID send(IncomingMessage message, UUID botId) throws Exception {
        switch (message.type) {
            case "text": {
                return sendText(message, botId);
            }
            case "attachment": {
                if (message.attachment.mimeType.startsWith("image")) {
                    return sendPicture(message, botId);
                }
                if (message.attachment.mimeType.startsWith("audio")) {
                    return sendAudio(message, botId);
                }

                return sendAttachment(message, botId);
            }
            case "poll": {
                if (message.poll.type.equals("create")) {
                    return sendNewPoll(message, botId);
                }
                if (message.poll.type.equals("confirmation")) {
                    return sendPollConfirmation(message, botId);
                }
                break;
            }
            case "call": {
                return sendCall(message, botId);
            }
        }

        return null;
    }

    @Nullable
    public UUID sendCall(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient wireClient = repo.getClient(botId)) {
            if (wireClient == null)
                return null;
            String content = message.call != null
                    ? mapper.writeValueAsString(message.call)
                    : "{\"version\":\"3.0\",\"type\":\"GROUPSTART\",\"sessid\":\"\",\"resp\":false}";
            final Calling calling = new Calling(content);
            wireClient.send(calling);
            return calling.getMessageId();
        }
    }

    @Nullable
    public UUID sendText(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient wireClient = repo.getClient(botId)) {
            if (wireClient == null)
                return null;

            MessageText text = new MessageText(message.text.data);
            text.setExpectsReadConfirmation(true);
            if (message.text.mentions != null) {
                for (Mention mention : message.text.mentions)
                    text.addMention(mention.userId, mention.offset, mention.length);
            }
            wireClient.send(text);
            return text.getMessageId();
        }
    }

    @Nullable
    private UUID sendAudio(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient wireClient = repo.getClient(botId)) {
            if (wireClient == null)
                return null;
            final byte[] bytes = base64Decode(message);

            final AudioPreview preview = new AudioPreview(
                    message.attachment.filename,
                    message.attachment.mimeType,
                    message.attachment.duration,
                    message.attachment.levels,
                    bytes.length);

            wireClient.send(preview);

            final AudioAsset audioAsset = new AudioAsset(bytes, preview);

            final AssetKey assetKey = wireClient.uploadAsset(audioAsset);
            audioAsset.setAssetToken(assetKey.token != null ? assetKey.token : "");
            audioAsset.setAssetKey(assetKey.key != null ? assetKey.key : "");

            wireClient.send(audioAsset);
            return audioAsset.getMessageId();
        }
    }

    @Nullable
    public UUID sendNewPoll(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient wireClient = repo.getClient(botId)) {
            if (wireClient == null)
                return null;
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

            wireClient.send(poll);

            return poll.getMessageId();
        }
    }

    @Nullable
    private UUID sendAttachment(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient wireClient = repo.getClient(botId)) {
            if (wireClient == null)
                return null;
            UUID messageId = UUID.randomUUID();

            final Attachment attachment = message.attachment;
            final byte[] decode = base64Decode(message);

            FileAssetPreview preview = new FileAssetPreview(attachment.filename,
                    attachment.mimeType,
                    decode.length,
                    messageId);
            FileAsset asset = new FileAsset(decode, attachment.mimeType, messageId);

            wireClient.send(preview);
            final AssetKey assetKey = wireClient.uploadAsset(asset);
            asset.setAssetKey(assetKey.key != null ? assetKey.key : "");
            asset.setAssetToken(assetKey.token != null ? assetKey.token : "");
            wireClient.send(asset);
            return messageId;
        }
    }

    @Nullable
    public UUID sendPollConfirmation(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient wireClient = repo.getClient(botId)) {
            if (wireClient == null)
                return null;
            ButtonActionConfirmation confirmation = new ButtonActionConfirmation(
                    message.poll.id,
                    message.poll.offset.toString());

            Logger.info("poll.confirmation: pollId: %s, offset: %s", message.poll.id, message.poll.offset);

            wireClient.send(confirmation, message.poll.userId);

            return confirmation.getMessageId();
        }
    }

    @Nullable
    public Conversation getConversation(UUID botId) throws IOException, CryptoException {
        try (WireClient wireClient = repo.getClient(botId)) {
            if (wireClient == null)
                return null;
            return wireClient.getConversation();
        }
    }

    @Nullable
    private UUID sendPicture(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient wireClient = repo.getClient(botId)) {
            if (wireClient == null)
                return null;
            final Picture picture = new Picture(base64Decode(message), message.attachment.mimeType);
            final AssetKey assetKey = wireClient.uploadAsset(picture);
            picture.setAssetToken(assetKey.token);
            picture.setAssetKey(assetKey.key);
            wireClient.send(picture);
            return picture.getMessageId();
        }
    }

    @Nullable
    public AssetKey uploadAsset(IAsset asset, UUID botId) throws Exception {
        try (WireClient wireClient = repo.getClient(botId)) {
            if (wireClient == null)
                return null;
            return wireClient.uploadAsset(asset);
        }
    }

    @Nullable
    public UUID send(IGeneric message, UUID botId) throws Exception {
        try (WireClient wireClient = repo.getClient(botId)) {
            if (wireClient == null)
                return null;

            wireClient.send(message);
            return message.getMessageId();
        }
    }
}
