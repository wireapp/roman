// Wire
// Copyright (C) 2016 Wire Swiss GmbH
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see http://www.gnu.org/licenses/.
//

package com.wire.bots.ealarming;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.DAO.GroupsDAO;
import com.wire.bots.ealarming.DAO.TemplateDAO;
import com.wire.bots.ealarming.DAO.UserDAO;
import com.wire.bots.ealarming.model.Config;
import com.wire.bots.ealarming.resources.*;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.skife.jdbi.v2.DBI;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.security.Key;
import java.util.EnumSet;

public class Service extends Server<Config> {
    public static Service instance;
    public Config config;
    public Key key;

    private DBI jdbi;

    public static void main(String[] args) throws Exception {
        Service instance = new Service();
        instance.run(args);
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        super.initialize(bootstrap);
        instance = (Service) bootstrap.getApplication();
    }

    @Override
    protected void initialize(Config config, Environment env) {
        this.jdbi = new DBIFactory().build(environment, config.database, "postgresql");
        this.config = config;
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // Enable CORS headers
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }

    @Override
    protected MessageHandlerBase createHandler(Config config, Environment env) {
        return new MessageHandler(jdbi);
    }

    @Override
    protected void onRun(Config config, Environment env) {
        final TemplateDAO templateDAO = jdbi.onDemand(TemplateDAO.class);
        final Alert2UserDAO alert2UserDAO = jdbi.onDemand(Alert2UserDAO.class);
        final UserDAO userDAO = jdbi.onDemand(UserDAO.class);
        final GroupsDAO groupsDAO = jdbi.onDemand(GroupsDAO.class);

        addResource(new AlertResource(jdbi), env);
        addResource(new TemplateResource(templateDAO), env);
        addResource(new UsersResource(alert2UserDAO), env);
        addResource(new SearchResource(userDAO, groupsDAO), env);
        addResource(new GroupsResource(groupsDAO), env);
        addResource(new BroadcastResource(jdbi, getRepo()), env);
        addResource(new ReportResource(alert2UserDAO), env);
        addResource(new AttachmentsResource(jdbi), env);
        addResource(new SigninResource(jdbi), env);
    }
}
