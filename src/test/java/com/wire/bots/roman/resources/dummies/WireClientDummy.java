package com.wire.bots.roman.resources.dummies;

import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.WireClientBase;
import com.wire.bots.sdk.assets.IAsset;
import com.wire.bots.sdk.assets.IGeneric;
import com.wire.bots.sdk.models.AssetKey;
import com.wire.bots.sdk.models.otr.PreKey;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.User;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class WireClientDummy extends WireClientBase implements WireClient {
    public WireClientDummy() {
        super(null, null, null);
    }

    @Override
    public void send(IGeneric message) {
    }

    @Override
    public UUID sendText(String txt) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendText(String txt, long expires) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendText(String txt, UUID mention) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendDirectText(String txt, UUID userId) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendLinkPreview(String url, String title, IGeneric image) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendDirectLinkPreview(String url, String title, IGeneric image, UUID userId) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendPicture(byte[] bytes, String mimeType) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendDirectPicture(byte[] bytes, String mimeType, UUID userId) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendPicture(IGeneric image) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendDirectPicture(IGeneric image, UUID userId) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendAudio(byte[] bytes, String name, String mimeType, long duration) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendVideo(byte[] bytes, String name, String mimeType, long duration, int h, int w) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendFile(File file, String mime) {
        return null;
    }

    @Override
    public UUID sendDirectFile(File file, String mime, UUID userId) {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendDirectFile(IGeneric preview, IGeneric asset, UUID userId) {
        return UUID.randomUUID();
    }

    @Override
    public UUID ping() {
        return UUID.randomUUID();
    }

    @Override
    public UUID sendReaction(UUID msgId, String emoji) {
        return UUID.randomUUID();
    }

    @Override
    public UUID deleteMessage(UUID msgId) {
        return UUID.randomUUID();
    }

    @Override
    public UUID editMessage(UUID replacingMessageId, String text) {
        return UUID.randomUUID();
    }

    @Override
    public byte[] downloadAsset(String assetKey, String assetToken, byte[] sha256Challenge, byte[] otrKey) {
        return new byte[0];
    }

    @Override
    public User getSelf() {
        return new User();
    }

    @Override
    public Collection<User> getUsers(Collection<UUID> userIds) {
        return Collections.singleton(getUser(Const.USER_ID));
    }

    @Override
    public User getUser(UUID userId) {
        User user = new User();
        user.id = userId;
        return user;
    }

    @Override
    public Conversation getConversation() {
        Conversation conversation = new Conversation();
        conversation.id = Const.CONV_ID;
        return conversation;
    }

    @Override
    public void acceptConnection(UUID user) {

    }

    @Override
    public void uploadPreKeys(ArrayList<PreKey> preKeys) {

    }

    @Override
    public ArrayList<Integer> getAvailablePrekeys() {
        return new ArrayList<>();
    }

    @Override
    public byte[] downloadProfilePicture(String assetKey) {
        return new byte[0];
    }

    @Override
    public AssetKey uploadAsset(IAsset asset) {
        return new AssetKey();
    }

    @Override
    public void call(String content) {

    }

    @Override
    public void close() {
    }
}
