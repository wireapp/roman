package com.wire.bots.ealarming.commands;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaworks.crypto.SCryptUtil;
import com.wire.bots.ealarming.DAO.AdminsDAO;
import com.wire.bots.ealarming.model.Config;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.skife.jdbi.v2.DBI;

public class SignUpCommand extends ConfiguredCommand<Config> {
    public SignUpCommand() {
        super("admin", "Signs up new Admin with email address and password");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-e", "--email")
                .dest("email")
                .type(String.class)
                .required(true)
                .help("Email address");

        subparser.addArgument("-p", "--password")
                .dest("password")
                .type(String.class)
                .required(true)
                .help("Password");
    }

    @Override
    public void run(Bootstrap<Config> bootstrap, Namespace namespace, Config config) {
        Environment environment = new Environment("command",
                new ObjectMapper(),
                null,
                new MetricRegistry(),
                null);
        DBI jdbi = new DBIFactory().build(environment, config.database, "postgresql");
        AdminsDAO adminsDAO = jdbi.onDemand(AdminsDAO.class);

        String email = namespace.getString("email");
        String password = namespace.getString("password");

        String hash = SCryptUtil.scrypt(password, 16384, 8, 1);

        int id = adminsDAO.insert(email, hash);

        System.out.printf("New Admin created. id: %d, email: %s, hash: %s\n", id, email, hash);
    }
}
