package com.wire.bots.roman;

import com.wire.bots.roman.model.OutgoingMessage;
import com.wire.bots.sdk.tools.Logger;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/roman/await/{access_token}", encoders = MessageEncoder.class)
public class WebSocket {
    private final static ConcurrentHashMap<UUID, Session> sessions = new ConcurrentHashMap<>();// ProviderId, Session,

    static boolean send(UUID providerId, OutgoingMessage message) throws IOException, EncodeException {
        Session session = sessions.get(providerId);
        if (session != null && session.isOpen()) {
            Logger.info("Sending message (%s) over wss to %s", message.type, providerId);

            session.getBasicRemote().sendObject(message);
            return true;
        }
        return false;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("access_token") String token) {
        String subject = Tools.validateToken(token);
        UUID providerId = UUID.fromString(subject);

        sessions.put(providerId, session);

        Logger.debug("Session %s connected. provider: %s", session.getId(), providerId);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        Logger.debug("%s disconnected", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.warning("%s error: %s", session.getId(), throwable);
    }
}