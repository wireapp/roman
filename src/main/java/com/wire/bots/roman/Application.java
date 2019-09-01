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

package com.wire.bots.roman;

import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.resources.*;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.jsonwebtoken.security.Keys;
import org.skife.jdbi.v2.DBI;

import java.security.Key;

public class Service extends Server<Config> {
    private static Service instance;

    private Key key;
    private DBI jdbi;

    public static void main(String[] args) throws Exception {
        new Service().run(args);
    }

    public static Key getKey() {
        return instance.key;
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        super.initialize(bootstrap);
        instance = (Service) bootstrap.getApplication();
    }

    @Override
    protected MessageHandlerBase createHandler(Config config, Environment env) {
        return new MessageHandler(jdbi);
    }

    @Override
    protected void initialize(Config config, Environment env) {
        this.key = Keys.hmacShaKeyFor(config.key.getBytes());
        this.jdbi = new DBIFactory().build(environment, config.database, "postgresql");
    }

    @Override
    protected void onRun(Config config, Environment env) {
        addResource(new ProviderResource(jdbi, getClient()), env);
        addResource(new ServiceResource(jdbi, getClient()), env);
    }
}
