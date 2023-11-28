package com.wire.bots.roman.integrations;

import com.wire.bots.roman.Application;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.Tools;
import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.model.Provider;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import jakarta.ws.rs.client.Client;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.*;
import java.net.URI;
import java.util.UUID;

public class WebSocketTest {
    URI wss = null;

    @ClientEndpoint(decoders = WebSocketTest._Decoder.class)
    public class WebsocketClientEndpoint {
        Session session;

        public WebsocketClientEndpoint() {
            try {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                session = container.connectToServer(this, wss);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @OnOpen
        public void onOpen(Session session) {
            System.out.printf("Websocket open: %s\n", session.getId());
        }

        @OnMessage
        public void onMessage(Object payload) {
        }

        @OnClose
        public void onClose(Session closed, CloseReason reason) {
            System.out.printf("Websocket closed: %s: reason: %s\n", closed.getId(), reason.getCloseCode());
        }
    }

    private static final DropwizardTestSupport<Config> SUPPORT = new DropwizardTestSupport<>(
            Application.class, "roman.yaml",
            ConfigOverride.config("key", "TcZA2Kq4GaOcIbQuOvasrw34321cZAfLW4Ga54fsds43hUuOdcdm42"),
            ConfigOverride.config("romanPubKeyBase64", "pubkey.PEM"));
    private Client client;
    private Jdbi jdbi;

    @Before
    public void beforeClass() throws Exception {
        SUPPORT.before();
        Application app = SUPPORT.getApplication();
        client = app.getClient();
        jdbi = app.getJdbi();
    }

    @After
    public void afterClass() {
        SUPPORT.after();
    }

    @Test
    public void connectTest() throws Exception {
        final UUID botId = UUID.randomUUID();
        final UUID providerId = UUID.randomUUID();
        final String serviceAuth = Tools.generateToken(botId);
        final UUID serviceId = UUID.randomUUID();

        final String email = String.format("%s@email.com", serviceAuth);

        // Create some fake provider and service
        ProvidersDAO providersDAO = jdbi.onDemand(ProvidersDAO.class);
        providersDAO.insert("Test Provider", providerId, email, "hash", "password");
        providersDAO.update(providerId, null, serviceAuth, serviceId, "Test Service", null);

        Provider provider = providersDAO.get(providerId);

        final String wssUrl = "ws://localhost:8080";
        String appKey = provider.serviceAuth;

        wss = client.target(wssUrl)
                .path("await")
                .path(appKey)
                .getUri();

        final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint();

        Thread.sleep(2000);

        clientEndPoint.session.close();

        Thread.sleep(2000);
    }

    public static class _Decoder implements Decoder.Text<Object> {
        @Override
        public Object decode(String s) {
            return new Object();
        }

        @Override
        public boolean willDecode(String s) {
            return s.startsWith("{") && s.endsWith("}");
        }

        @Override
        public void init(EndpointConfig config) {

        }

        @Override
        public void destroy() {

        }
    }
}
