package com.wire.bots.roman.resources.dummies;

import com.wire.xenon.WireClient;
import com.wire.xenon.WireClientBase;
import com.wire.xenon.assets.IGeneric;
import com.wire.xenon.backend.models.Conversation;
import com.wire.xenon.backend.models.User;

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
    public ArrayList<Integer> getAvailablePrekeys() {
        return new ArrayList<>();
    }

    @Override
    public byte[] downloadProfilePicture(String assetKey) {
        return new byte[0];
    }

    @Override
    public void close() {
    }
}
