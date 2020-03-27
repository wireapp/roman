package com.wire.bots.roman.resources.dummies;

import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.WireClientBase;
import com.wire.bots.sdk.assets.IAsset;
import com.wire.bots.sdk.assets.IGeneric;
import com.wire.bots.sdk.exceptions.HttpException;
import com.wire.bots.sdk.models.AssetKey;
import com.wire.bots.sdk.models.otr.PreKey;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class WireClientDummy extends WireClientBase implements WireClient {
    public WireClientDummy() {
        super(null, null, null);
    }

    @Override
    public UUID sendText(String txt) throws Exception {
        return null;
    }

    @Override
    public UUID sendText(String txt, long expires) throws Exception {
        return null;
    }

    @Override
    public UUID sendText(String txt, UUID mention) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectText(String txt, UUID userId) throws Exception {
        return null;
    }

    @Override
    public UUID sendLinkPreview(String url, String title, IGeneric image) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectLinkPreview(String url, String title, IGeneric image, UUID userId) throws Exception {
        return null;
    }

    @Override
    public UUID sendPicture(byte[] bytes, String mimeType) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectPicture(byte[] bytes, String mimeType, UUID userId) throws Exception {
        return null;
    }

    @Override
    public UUID sendPicture(IGeneric image) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectPicture(IGeneric image, UUID userId) throws Exception {
        return null;
    }

    @Override
    public UUID sendAudio(byte[] bytes, String name, String mimeType, long duration) throws Exception {
        return null;
    }

    @Override
    public UUID sendVideo(byte[] bytes, String name, String mimeType, long duration, int h, int w) throws Exception {
        return null;
    }

    @Override
    public UUID sendFile(File file, String mime) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectFile(File file, String mime, UUID userId) throws Exception {
        return null;
    }

    @Override
    public UUID sendDirectFile(IGeneric preview, IGeneric asset, UUID userId) throws Exception {
        return null;
    }

    @Override
    public UUID ping() throws Exception {
        return null;
    }

    @Override
    public UUID sendReaction(UUID msgId, String emoji) throws Exception {
        return null;
    }

    @Override
    public UUID deleteMessage(UUID msgId) throws Exception {
        return null;
    }

    @Override
    public UUID editMessage(UUID replacingMessageId, String text) throws Exception {
        return null;
    }

    @Override
    public byte[] downloadAsset(String assetKey, String assetToken, byte[] sha256Challenge, byte[] otrKey) throws Exception {
        return new byte[0];
    }

    @Override
    public User getSelf() throws HttpException {
        return null;
    }

    @Override
    public Collection<User> getUsers(Collection<UUID> userIds) throws HttpException {
        return Collections.singleton(getUser(Const.USER_ID));
    }

    @Override
    public User getUser(UUID userId) throws HttpException {
        User user = new User();
        user.id = userId;
        return user;
    }

    @Override
    public Conversation getConversation() throws IOException {
        Conversation conversation = new Conversation();
        conversation.id = Const.CONV_ID;
        return conversation;
    }

    @Override
    public void acceptConnection(UUID user) throws Exception {

    }

    @Override
    public void uploadPreKeys(ArrayList<PreKey> preKeys) throws IOException {

    }

    @Override
    public ArrayList<Integer> getAvailablePrekeys() {
        return null;
    }

    @Override
    public byte[] downloadProfilePicture(String assetKey) throws Exception {
        return new byte[0];
    }

    @Override
    public AssetKey uploadAsset(IAsset asset) throws Exception {
        return null;
    }

    @Override
    public void call(String content) throws Exception {

    }

    @Override
    public void close() throws IOException {
    }
}
