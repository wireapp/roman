package com.wire.bots.ealarming.DAO;

import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public interface User2BotDAO {

    @SqlQuery("SELECT bot_id FROM User2Bot WHERE user_id = :userId")
    @RegisterMapper(_Mapper.class)
    UUID get(@Bind("userId") UUID userId);

    @SqlUpdate("INSERT INTO User2Bot(bot_id, user_id) VALUES (:botId, :userId)")
    int insert(@Bind("userId") UUID userId,
               @Bind("botId") UUID botId
    );

    @SqlUpdate("DELETE FROM User2Bot WHERE bot_id = :botId")
    int delete(@Bind("botId") UUID botId);

    class _Mapper implements ResultSetMapper<UUID> {
        @Override
        @Nullable
        public UUID map(int i, ResultSet rs, StatementContext statementContext) {
            try {
                return getUuid(rs, "bot_id");
            } catch (SQLException e) {
                Logger.error("User2BotDAOMapper: i: %d, e: %s", i, e);
                return null;
            }
        }

        private UUID getUuid(ResultSet rs, String name) throws SQLException {
            UUID contact = null;
            Object rsObject = rs.getObject(name);
            if (rsObject != null)
                contact = (UUID) rsObject;
            return contact;
        }
    }
}
