package com.wire.bots.roman.integrations;

import com.wire.bots.roman.Application;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.model.Provider;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class DatabaseTest {
    private static final DropwizardTestSupport<Config> SUPPORT = new DropwizardTestSupport<>(
            Application.class, "roman.yaml",
            ConfigOverride.config("key", "TcZA2Kq4GaOcIbQuOvasrw34321cZAfLW4Ga54fsds43hUuOdcdm42"));
    private Jdbi jdbi;

    @Before
    public void beforeClass() throws Exception {
        SUPPORT.before();
        Application app = SUPPORT.getApplication();
        jdbi = app.getJdbi();
    }

    @After
    public void afterClass() {
        SUPPORT.after();
    }


    @Test
    public void testProviderDAO() {
        final ProvidersDAO providersDAO = jdbi.onDemand(ProvidersDAO.class);

        final UUID id = UUID.randomUUID();
        final String name = "name";
        final String email = "email@wire.com";
        final String hash = "hash";
        final String password = "password";
        final int insert = providersDAO.insert(name, id, email, hash, password);
        assert insert == 1;

        Provider provider = providersDAO.get(id);
        assert provider != null;
        assert provider.name.equals(name);
        assert provider.hash.equals(hash);
        assert provider.password.equals(password);
        assert provider.id.equals(id);
        assert provider.email.equals(email);

        provider = providersDAO.get(email);
        assert provider != null;
        assert provider.name.equals(name);
        assert provider.hash.equals(hash);
        assert provider.password.equals(password);
        assert provider.id.equals(id);
        assert provider.email.equals(email);

        final String url = "url";
        final String auth = "auth";
        final UUID serviceId = UUID.randomUUID();
        final String service_name = "service name";
        int update = providersDAO.update(id, url, auth, serviceId, service_name);
        assert update == 1;

        provider = providersDAO.getByAuth(auth);
        assert provider != null;
        assert provider.serviceAuth.equals(auth);
        assert provider.serviceUrl.equals(url);
        assert provider.serviceId.equals(serviceId);
        assert provider.serviceName.equals(service_name);

        final String newURL = "newURL";
        update = providersDAO.updateUrl(id, newURL);
        assert update == 1;

        provider = providersDAO.get(id);
        assert provider != null;
        assert provider.serviceUrl.equals(newURL);

        final String newName = "new service name";
        update = providersDAO.updateServiceName(id, newName);
        assert update == 1;

        provider = providersDAO.get(id);
        assert provider != null;
        assert provider.serviceName.equals(newName);
    }
}
