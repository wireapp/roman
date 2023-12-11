package com.wire.bots.roman.commands;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.ProviderClient;
import com.wire.bots.roman.Tools;
import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.model.Provider;
import com.wire.xenon.tools.Logger;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.core.cli.ConfiguredCommand;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.util.List;

public class UpdateCertCommand extends ConfiguredCommand<Config> {
    public UpdateCertCommand() {
        super("cert", "Updates certificates for all services");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
    }

    @Override
    public void run(Bootstrap<Config> bootstrap, Namespace namespace, Config config) {
        Environment environment = new Environment("UpdateCertCommand");

        Client client = new JerseyClientBuilder(environment)
                .using(config.getJerseyClient())
                .withProvider(JacksonJsonProvider.class)
                .build(getName());

        ProviderClient providerClient = new ProviderClient(client, config.apiHost);

        final Jdbi jdbi = Jdbi.create(config.database.build(environment.metrics(), getName()))
                .installPlugin(new SqlObjectPlugin());

        ProvidersDAO providersDAO = jdbi.onDemand(ProvidersDAO.class);

        String pubkey = Tools.decodeBase64(config.romanPubKeyBase64);

        Logger.info("\nCert:\n%s\n\n", pubkey);

        List<Provider> providers = providersDAO.selectAll();
        Logger.info("Updating cert for %d providers...\n\n", providers.size());

        for (Provider provider : providers) {
            if (provider.serviceId == null) {
                Logger.info("Skipping provider: %s, name: %s\n", provider.id, provider.name);
                continue;
            }

            Response login = providerClient.login(provider.email, provider.password);

            if (login.getStatus() >= 400) {
                Logger.info("Failed to login for provider: %s, status: %d, err: %s\n",
                        provider.id,
                        login.getStatus(),
                        login.readEntity(String.class));
                continue;
            }

            NewCookie cookie = login.getCookies().get("zprovider");

            updateCert(providerClient, pubkey, provider, cookie);
            updateURL(providerClient, provider, config.domain, cookie);
            // slow down a bit, BE will otherwise rate limit us
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Logger.exception("Sleep interrupted.", e);
            }
        }
    }

    private void updateCert(ProviderClient providerClient, String pubkey, Provider provider, NewCookie cookie) {
        try {
            Response response = providerClient.updateServicePubKey(cookie, provider.serviceId, provider.password, pubkey);
            Logger.info("Updated cert for provider: %s, name: %s. Status: %d\n",
                    provider.id,
                    provider.name,
                    response.getStatus());
        } catch (Exception e) {
            Logger.exception("ERROR updateCert provider: %s, error: %s\n", e, provider.id, e.getMessage());
        }
    }

    private void updateURL(ProviderClient providerClient, Provider provider, String url, NewCookie cookie) {
        try {
            Response response = providerClient.updateServiceURL(cookie, provider.serviceId, provider.password, url);

            Logger.info("Updated URL for provider: %s, name: %s. Status: %d\n",
                    provider.id,
                    provider.name,
                    response.getStatus());
            // reenable when the URL was changed
            providerClient.enableService(cookie, provider.serviceId, provider.password);

            Logger.info("Service enabled: %s, name: %s. Status: %d\n",
                    provider.id,
                    provider.name,
                    response.getStatus());
        } catch (Exception e) {
            Logger.exception("ERROR updateURL provider: %s, error: %s\n", e, provider.id, e.getMessage());
        }
    }
}
