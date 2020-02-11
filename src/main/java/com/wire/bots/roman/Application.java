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

import com.wire.bots.roman.commands.UpdateCertCommand;
import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.resources.BroadcastResource;
import com.wire.bots.roman.resources.ConversationResource;
import com.wire.bots.roman.resources.ProviderResource;
import com.wire.bots.roman.resources.ServiceResource;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;
import io.jsonwebtoken.security.Keys;
import org.skife.jdbi.v2.DBI;

import java.security.Key;

public class Application extends Server<Config> {
    private static Application instance;

    private Key key;
    private DBI jdbi;

    public static void main(String[] args) throws Exception {
        new Application().run(args);
    }

    public static Key getKey() {
        return instance.key;
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        super.initialize(bootstrap);
        instance = (Application) bootstrap.getApplication();

        WebsocketBundle bundle = new WebsocketBundle(WebSocket.class);
        bootstrap.addBundle(bundle);

        bootstrap.addCommand(new UpdateCertCommand());
    }

    @Override
    protected MessageHandlerBase createHandler(Config config, Environment env) {
        return new MessageHandler(jdbi, getClient());
    }

    public static Application getInstance() {
        return instance;
    }

    @Override
    protected void initialize(Config config, Environment env) {
        this.key = Keys.hmacShaKeyFor(config.key.getBytes());
        this.jdbi = new DBIFactory().build(environment, config.database, "roman");
    }

    @Override
    protected void onRun(Config config, Environment env) {
        ProviderClient providerClient = new ProviderClient(getClient());

        addResource(new ProviderResource(jdbi, providerClient));
        addResource(new ServiceResource(jdbi, providerClient));
        addResource(new ConversationResource(getRepo()));
        addResource(new BroadcastResource(jdbi, getRepo()));
    }
}
