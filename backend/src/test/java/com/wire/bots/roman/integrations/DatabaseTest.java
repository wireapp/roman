package com.wire.bots.roman.integrations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.Application;
import com.wire.bots.roman.DAO.BroadcastDAO;
import com.wire.bots.roman.DAO.OutgoingMessageDAO;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.model.Attachment;
import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.model.OutgoingMessage;
import com.wire.bots.roman.model.Provider;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;

import static com.wire.bots.roman.resources.dummies.Const.ROMAN_TEST_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DatabaseTest {
    private static final DropwizardTestSupport<Config> SUPPORT = new DropwizardTestSupport<>(
            Application.class, ROMAN_TEST_CONFIG, new ResourceConfigurationSourceProvider()
    );
    private Jdbi jdbi;

    @BeforeEach
    public void beforeClass() throws Exception {
        SUPPORT.before();
        Application app = SUPPORT.getApplication();
        jdbi = app.getJdbi();
    }

    @AfterEach
    public void afterClass() {
        SUPPORT.after();
    }

    @Test
    public void testProviderDAO() {
        final ProvidersDAO providersDAO = jdbi.onDemand(ProvidersDAO.class);

        final UUID providerId = UUID.randomUUID();
        final String name = "name";
        final String email = "email@wire.com";
        final String hash = "hash";
        final String password = "password";
        final int insert = providersDAO.insert(name, providerId, email, hash, password);
        assertThat(insert).isEqualTo(1);

        Provider provider = providersDAO.get(providerId);
        assertThat(provider).isNotNull();
        assertThat(provider.name).isEqualTo(name);
        assertThat(provider.hash).isEqualTo(hash);
        assertThat(provider.password).isEqualTo(password);
        assertThat(provider.id).isEqualTo(providerId);
        assertThat(provider.email).isEqualTo(email);

        provider = providersDAO.get(email);
        assertThat(provider).isNotNull();
        assertThat(provider.name).isEqualTo(name);
        assertThat(provider.hash).isEqualTo(hash);
        assertThat(provider.password).isEqualTo(password);
        assertThat(provider.id).isEqualTo(providerId);
        assertThat(provider.email).isEqualTo(email);

        final String url = "url";
        final String auth = "auth";
        final UUID serviceId = UUID.randomUUID();
        final String service_name = "service name";
        final String prefix = "/";

        int update = providersDAO.update(providerId, url, auth, serviceId, service_name, prefix);
        assertThat(update).isEqualTo(1);

        provider = providersDAO.getByAuth(auth);
        assertThat(provider).isNotNull();
        assertThat(provider.serviceAuth).isEqualTo(auth);
        assertThat(provider.serviceUrl).isEqualTo(url);
        assertThat(provider.serviceId).isEqualTo(serviceId);
        assertThat(provider.serviceName).isEqualTo(service_name);
        assertThat(provider.commandPrefix).isEqualTo(prefix);

        final String newURL = "newURL";
        update = providersDAO.updateUrl(providerId, newURL);
        assertThat(update).isEqualTo(1);

        provider = providersDAO.get(providerId);
        assertThat(provider).isNotNull();
        assertThat(provider.serviceUrl).isEqualTo(newURL);

        final String newName = "new service name";
        update = providersDAO.updateServiceName(providerId, newName);
        assertThat(update).isEqualTo(1);

        provider = providersDAO.get(providerId);
        assertThat(provider).isNotNull();
        assertThat(provider.serviceName).isEqualTo(newName);

        final String newPrefix = "@";
        update = providersDAO.updateServicePrefix(providerId, newPrefix);
        assertThat(update).isEqualTo(1);

        provider = providersDAO.get(providerId);
        assertThat(provider).isNotNull();
        assertThat(provider.commandPrefix).isEqualTo(newPrefix);

        final int deleteService = providersDAO.deleteService(providerId);
        provider = providersDAO.get(providerId);
    }

    @Test
    public void testBroadcastDAO() {
        final BroadcastDAO broadcastDAO = jdbi.onDemand(BroadcastDAO.class);

        final UUID providerId = UUID.randomUUID();
        final UUID broadcastId = UUID.randomUUID();
        final UUID botId = UUID.randomUUID();
        final UUID messageId = UUID.randomUUID();

        final int insert1 = broadcastDAO.insert(broadcastId, botId, providerId, messageId, 0);
        assertThat(insert1).isEqualTo(1);

        int insertStatus = broadcastDAO.insertStatus(messageId, 1);
        assertThat(insertStatus).isEqualTo(1);
        insertStatus = broadcastDAO.insertStatus(messageId, 2);
        assertThat(insertStatus).isEqualTo(1);
        insertStatus = broadcastDAO.insertStatus(messageId, 3);
        assertThat(insertStatus).isEqualTo(1);

        final UUID get = broadcastDAO.getBroadcastId(providerId);
        assertThat(get).isNotNull();
        assertThat(get).isEqualTo(broadcastId);

        final List<BroadcastDAO.Pair> report = broadcastDAO.report(broadcastId);

        final UUID broadcastId2 = UUID.randomUUID();
        final UUID botId2 = UUID.randomUUID();
        final UUID messageId2 = UUID.randomUUID();
        final int insert2 = broadcastDAO.insert(broadcastId2, botId2, providerId, messageId2, 0);
        assertThat(insert2).isEqualTo(1);

        final UUID get2 = broadcastDAO.getBroadcastId(providerId);
        assertThat(get2).isNotNull();
        assertThat(get2).isEqualTo(broadcastId2);
    }

    @Test
    public void testOutgoingMessageDAO() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final OutgoingMessageDAO outgoingMessageDAO = jdbi.onDemand(OutgoingMessageDAO.class);
        OutgoingMessage message = new OutgoingMessage();
        message.messageId = UUID.randomUUID();
        message.token = "token";
        message.attachment = new Attachment();
        message.attachment.data = "data";

        outgoingMessageDAO.insert(message.messageId, mapper.writeValueAsString(message));

        final OutgoingMessage challenge = outgoingMessageDAO.get(message.messageId);
        assertThat(challenge).isNotNull();
        assertThat(challenge.messageId).isEqualTo(message.messageId);

        outgoingMessageDAO.delete(message.messageId);
    }
}