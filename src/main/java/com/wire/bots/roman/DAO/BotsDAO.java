package com.wire.bots.roman.DAO;

import com.wire.bots.roman.DAO.mappers.BotsMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;
import java.util.UUID;

public interface BotsDAO {
    @SqlUpdate("INSERT INTO Bots (id, provider) VALUES (:bot, :provider)")
    int insert(@Bind("bot") UUID bot,
               @Bind("provider") UUID provider);

    @SqlQuery("SELECT provider AS uuid FROM Bots WHERE id = :bot")
    @RegisterMapper(BotsMapper.class)
    UUID getProviderId(@Bind("bot") UUID bot);


    @SqlQuery("SELECT id AS uuid FROM Bots WHERE provider = :providerId")
    @RegisterMapper(BotsMapper.class)
    List<UUID> getBotIds(@Bind("providerId") UUID providerId);

    @SqlUpdate("DELETE FROM Bots WHERE id = :botId")
    int remove(@Bind("botId") UUID botId);
}
