package com.wire.bots.roman.DAO;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.UUID;

public interface BotsDAO {
    @SqlUpdate("INSERT INTO Bots (id, url, token) " +
            "VALUES (:bot, :url, :token)")
    int insert(@Bind("bot") UUID bot,
               @Bind("url") String url,
               @Bind("token") String token);

    @SqlQuery("SELECT url FROM Bots WHERE id = :bot")
    String getUrl(@Bind("bot") UUID bot);

    @SqlQuery("SELECT token FROM Bots WHERE id = :bot")
    String getToken(@Bind("bot") UUID bot);
}
