package com.wire.bots.roman;

import com.wire.bots.roman.model.OutgoingMessage;
import com.wire.xenon.tools.Logger;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.wire.bots.roman.Const.APP_KEY;

@ServerEndpoint(value = "/await/{app-key}", encoders = MessageEncoder.class)
public class WebSocket {
    private final static ConcurrentHashMap<UUID, Session> sessions = new ConcurrentHashMap<>();// ProviderId, Session,

    static boolean send(UUID providerId, OutgoingMessage message) throws IOException, EncodeException {
        Session session = sessions.get(providerId);
        if (session != null && session.isOpen()) {
            Logger.debug("Sending message (%s) over wss to provider: %s, bot: %s",
                    message.type,
                    providerId,
                    message.botId);

            session.getBasicRemote().sendObject(message);
            return true;
        }
        return false;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam(APP_KEY) String token) {
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
        Logger.exception(throwable, "%s error: %s", session.getId(), throwable.getMessage());
    }
}
