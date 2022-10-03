package com.wire.bots.roman.integrations;

import com.wire.bots.roman.Application;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.Tools;
import com.wire.bots.roman.model.*;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.ClientEndpoint;

import javax.websocket.*;
import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.UUID;

public class WebSocketTest {
    @ClientEndpoint
    static class WebsocketClientEndpoint {
        public WebsocketClientEndpoint(URI endpointURI) {
            try {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                container.connectToServer(this, endpointURI);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
    public void connectTest() {
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

        URI wss = client.target(wssUrl)
                .path("await")
                .path(appKey)
                .getUri();

        final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(wss);

    }
}
