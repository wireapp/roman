package com.wire.bots.roman.integrations;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.model.Provider;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import java.util.UUID;

public class DatabaseTest {
    private static ProvidersDAO providersDAO;

    static {
        Config.Database db = new Config.Database();
        db.setUrl("jdbc:postgresql://localhost/roman");
        db.setDriverClass("org.postgresql.Driver");
        Environment env = new Environment("DatabaseTest", new ObjectMapper(), null, new MetricRegistry(), null);
        final DBI dbi = new DBIFactory().build(env, db, "roman");

        providersDAO = dbi.onDemand(ProvidersDAO.class);
    }

    @Test
    public void testProviderDAO() {
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
