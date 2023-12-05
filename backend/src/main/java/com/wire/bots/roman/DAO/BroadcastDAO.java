package com.wire.bots.roman.DAO;

import com.wire.bots.roman.DAO.mappers.UUIDMapper;
import jakarta.annotation.Nullable;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface BroadcastDAO {
    @SqlUpdate("INSERT INTO Broadcast (broadcast_id, bot_id, provider, message_status, message_id) " +
            "VALUES (:broadcastId, :botId, :provider, :status, :messageId) " +
            "ON CONFLICT(broadcast_id, message_id, message_status) DO NOTHING")
    int insert(@Bind("broadcastId") UUID broadcastId,
               @Bind("botId") UUID botId,
               @Bind("provider") UUID provider,
               @Bind("messageId") UUID messageId,
               @Bind("status") int status);

    @SqlUpdate("INSERT INTO Broadcast (broadcast_id, bot_id, provider, message_status, message_id) " +
            "(SELECT B.broadcast_id, B.bot_id, B.provider, :status, B.message_id " +
            "FROM Broadcast AS B " +
            "WHERE message_id = :messageId FETCH FIRST ROW ONLY) " +
            "ON CONFLICT(broadcast_id, message_id, message_status) DO NOTHING")
    int insertStatus(@Bind("messageId") UUID messageId, @Bind("status") int status);

    @SqlQuery("SELECT message_status, count(*) AS count " +
            "FROM Broadcast " +
            "WHERE broadcast_id = :broadcastId " +
            "GROUP BY message_status")
    @RegisterColumnMapper(ReportMapper.class)
    List<Pair> report(@Bind("broadcastId") UUID broadcastId);

    @SqlQuery("SELECT broadcast_id AS uuid " +
            "FROM Broadcast " +
            "WHERE provider = :provider " +
            "ORDER BY created DESC " +
            "FETCH FIRST ROW ONLY")
    @RegisterColumnMapper(UUIDMapper.class)
    @Nullable
    UUID getBroadcastId(@Bind("provider") UUID provider);

    enum Type {
        SENT,
        DELIVERED,
        READ,
        FAILED
    }

    class Pair {
        public Type type;
        public int count;
    }

    class ReportMapper implements ColumnMapper<Pair> {
        @Override
        public Pair map(ResultSet rs, int columnNumber, StatementContext ctx) throws SQLException {
            Pair ret = new Pair();
            ret.type = Type.values()[rs.getInt("message_status")];
            ret.count = rs.getInt("count");
            return ret;
        }
    }
}
