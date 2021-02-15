package com.wire.bots.roman.DAO;

import com.wire.bots.roman.DAO.mappers.BotsMapper;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface BotsDAO {
    @SqlUpdate("INSERT INTO Bots (id, provider) VALUES (:bot, :provider)")
    int insert(@Bind("bot") UUID bot,
               @Bind("provider") UUID provider);

    @SqlQuery("SELECT provider AS uuid FROM Bots WHERE id = :bot")
    @RegisterColumnMapper(BotsMapper.class)
    UUID getProviderId(@Bind("bot") UUID bot);


    @SqlQuery("SELECT id AS uuid FROM Bots WHERE provider = :providerId")
    @RegisterColumnMapper(BotsMapper.class)
    List<UUID> getBotIds(@Bind("providerId") UUID providerId);

    @SqlUpdate("DELETE FROM Bots WHERE id = :botId")
    int remove(@Bind("botId") UUID botId);
}
