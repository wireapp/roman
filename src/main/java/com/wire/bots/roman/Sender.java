package com.wire.bots.roman;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.cryptobox.CryptoException;
import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.model.AssetMeta;
import com.wire.bots.roman.model.Attachment;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.Mention;
import com.wire.lithium.ClientRepo;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.*;
import com.wire.xenon.backend.models.Conversation;
import com.wire.xenon.models.AssetKey;
import com.wire.xenon.tools.Logger;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class Sender {
    private final ObjectMapper mapper = new ObjectMapper();

    private final ClientRepo repo;
    private final BotsDAO botsDAO;


    public Sender(ClientRepo repo, Jdbi jdbi) {
        this.repo = repo;
        botsDAO = jdbi.onDemand(BotsDAO.class);
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
        try (WireClient wireClient = getWireClient(botId)) {
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
        try (WireClient wireClient = getWireClient(botId)) {
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
        try (WireClient wireClient = getWireClient(botId)) {
            if (wireClient == null)
                return null;

            final Attachment attachment = message.attachment;

            final AudioPreview preview = new AudioPreview(
                    attachment.name,
                    attachment.mimeType,
                    attachment.duration,
                    attachment.levels,
                    attachment.size.intValue());

            wireClient.send(preview);

            AssetBase asset;
            if (message.attachment.meta != null) {
                asset = new AudioAsset(preview.getMessageId(), attachment.mimeType);
                setAssetMetadata(asset, message.attachment.meta);
            } else if (message.attachment.data != null) {
                final byte[] bytes = Base64.getDecoder().decode(message.attachment.data);
                asset = new AudioAsset(bytes, preview);
                uploadAssetData(wireClient, asset);
            } else {
                throw new Exception("Meta or Data need to be set");
            }

            wireClient.send(asset);
            return asset.getMessageId();
        }
    }

    @Nullable
    private UUID sendAttachment(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient wireClient = getWireClient(botId)) {
            if (wireClient == null)
                return null;

            final Attachment attachment = message.attachment;

            FileAssetPreview preview = new FileAssetPreview(attachment.name,
                    attachment.mimeType,
                    attachment.size,
                    UUID.randomUUID());

            wireClient.send(preview);

            AssetBase asset;
            if (message.attachment.meta != null) {
                asset = new FileAsset(preview.getMessageId(), attachment.mimeType);
                setAssetMetadata(asset, message.attachment.meta);
            } else if (message.attachment.data != null) {
                final byte[] bytes = Base64.getDecoder().decode(message.attachment.data);
                asset = new FileAsset(bytes, attachment.mimeType, preview.getMessageId());
                uploadAssetData(wireClient, asset);
            } else {
                throw new Exception("Meta or Data need to be set");
            }

            wireClient.send(asset);
            return asset.getMessageId();
        }
    }

    @Nullable
    private UUID sendPicture(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient wireClient = getWireClient(botId)) {
            if (wireClient == null)
                return null;

            final UUID messageId = UUID.randomUUID();
            final Attachment attachment = message.attachment;

            Picture asset;
            if (message.attachment.meta != null) {
                asset = new Picture(messageId, attachment.mimeType);
                setAssetMetadata(asset, message.attachment.meta);
                asset.setHeight(attachment.height);
                asset.setWidth(attachment.width);
                asset.setSize(attachment.size.intValue());
            } else if (message.attachment.data != null) {
                final byte[] bytes = Base64.getDecoder().decode(message.attachment.data);
                asset = new Picture(bytes, attachment.mimeType);
                uploadAssetData(wireClient, asset);
            } else {
                throw new Exception("Meta or Data need to be set");
            }

            wireClient.send(asset);
            return asset.getMessageId();
        }
    }

    private WireClient getWireClient(UUID botId) throws IOException, CryptoException {
        final WireClient wireClient = repo.getClient(botId);
        if (wireClient == null) {
            final int remove = botsDAO.remove(botId);
            Logger.info("Sender.getWireClient: botId: %s, Deleted: %d", botId, remove);
        }
        return wireClient;
    }

    @Nullable
    public UUID sendNewPoll(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient wireClient = getWireClient(botId)) {
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
    public UUID sendPollConfirmation(IncomingMessage message, UUID botId) throws Exception {
        try (WireClient wireClient = getWireClient(botId)) {
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
        try (WireClient wireClient = getWireClient(botId)) {
            if (wireClient == null)
                return null;
            return wireClient.getConversation();
        }
    }

    private void setAssetMetadata(AssetBase asset, AssetMeta meta) {
        asset.setAssetKey(meta.assetId);
        asset.setAssetToken(meta.assetToken);
        asset.setSha256(Base64.getDecoder().decode(meta.sha256));
        asset.setOtrKey(Base64.getDecoder().decode(meta.otrKey));
    }

    private void uploadAssetData(WireClient wireClient, AssetBase asset) throws Exception {
        final AssetKey assetKey = wireClient.uploadAsset(asset);
        asset.setAssetKey(assetKey.id);
        asset.setAssetToken(assetKey.token);
    }
}
