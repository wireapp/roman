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

import com.wire.bots.ealarming.DAO.*;
import com.wire.bots.ealarming.model.Config;
import com.wire.bots.ealarming.resources.*;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import com.wire.bots.sdk.tools.AuthValidator;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

public class Service extends Server<Config> {
    static Service instance;
    DBI jdbi;

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
        jdbi = new DBIFactory().build(environment, config.database, "postgresql");
    }

    @Override
    protected MessageHandlerBase createHandler(Config config, Environment env) {
        return new MessageHandler(jdbi);
    }

    @Override
    protected void onRun(Config config, Environment env) {
        final AlertDAO alertDAO = jdbi.onDemand(AlertDAO.class);
        final TemplateDAO templateDAO = jdbi.onDemand(TemplateDAO.class);
        final Alert2UserDAO alert2UserDAO = jdbi.onDemand(Alert2UserDAO.class);
        final UserDAO userDAO = jdbi.onDemand(UserDAO.class);
        final GroupsDAO groupsDAO = jdbi.onDemand(GroupsDAO.class);
        final User2BotDAO user2BotDAO = jdbi.onDemand(User2BotDAO.class);

        AuthValidator validator = new AuthValidator(config.auth);

        addResource(new AlertResource(alertDAO, alert2UserDAO, userDAO, validator), env);
        addResource(new TemplateResource(templateDAO, groupsDAO, validator), env);
        addResource(new UsersResource(alert2UserDAO, userDAO, validator), env);
        addResource(new SearchResource(userDAO, groupsDAO, validator), env);
        addResource(new GroupsResource(groupsDAO, validator), env);
        addResource(new BroadcastResource(alertDAO, alert2UserDAO, groupsDAO, user2BotDAO, getRepo(), validator), env);
        addResource(new ReportResource(alert2UserDAO, validator), env);

    }
}
