package com.wire.bots.roman.DAO;

import com.wire.bots.roman.DAO.mappers.BotsMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.UUID;

public interface BotsDAO {
    @SqlUpdate("INSERT INTO Bots (id, provider) VALUES (:bot, :provider)")
    int insert(@Bind("bot") UUID bot,
               @Bind("provider") UUID provider);

    @SqlQuery("SELECT provider FROM Bots WHERE id = :bot")
    @RegisterMapper(BotsMapper.class)
    UUID getProviderId(@Bind("bot") UUID bot);
}
