package com.wire.bots.roman.commands;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.ProviderClient;
import com.wire.bots.roman.Tools;
import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.model.Provider;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

public class UpdateCertCommand extends ConfiguredCommand<Config> {

    public UpdateCertCommand() {
        super("cert", "Updates certificates for all services");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-d", "--domain")
                .dest("domain")
                .type(String.class)
                .required(true)
                .help("Domain");
    }

    @Override
    public void run(Bootstrap<Config> bootstrap, Namespace namespace, Config config) throws IOException {
        Environment environment = new Environment("UpdateCertCommand");

        Client client = new JerseyClientBuilder(environment)
                .using(config.getJerseyClient())
                .withProvider(JacksonJsonProvider.class)
                .build(getName());

        ProviderClient providerClient = new ProviderClient(client);

        DBI jdbi = new DBIFactory().build(environment, config.database, "postgresql");
        ProvidersDAO providersDAO = jdbi.onDemand(ProvidersDAO.class);

        String hostname = namespace.getString("domain");

        String pubkey = Tools.getPubkey(hostname);

        System.out.printf("\nCert:\n%s\n\n", pubkey);

        List<Provider> providers = providersDAO.selectAll();
        System.out.printf("Updating %s cert for %d providers...\n\n", hostname, providers.size());

        for (Provider provider : providers) {
            updateCert(providerClient, pubkey, provider);
        }
    }

    private void updateCert(ProviderClient providerClient, String pubkey, Provider provider) {
        try {
            if (provider.serviceId == null) {
                System.out.printf("Skipping provider: %s, name: %s\n", provider.id, provider.name);
                return;
            }

            Response login = providerClient.login(provider.email, provider.password);

            if (login.getStatus() >= 400) {
                System.out.printf("Failed to login for provider: %s, status: %d, err: %s\n",
                        provider.id,
                        login.getStatus(),
                        login.readEntity(String.class));
                return;
            }

            NewCookie cookie = login.getCookies().get("zprovider");

            Response response = providerClient.updateServicePubKey(cookie, provider.serviceId, provider.password, pubkey);

            System.out.printf("Updated cert for provider: %s, name: %s. Status: %d\n",
                    provider.id,
                    provider.name,
                    response.getStatus());
        } catch (Exception e) {
            System.err.printf("ERROR updateCert provider: %s, error: %s\n", provider.id, e.getMessage());
            e.printStackTrace();
        }
    }
}
