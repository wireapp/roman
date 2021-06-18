package com.wire.bots.roman.DAO;

import com.wire.bots.roman.DAO.mappers.OutgoingMessageRSMapper;
import com.wire.bots.roman.model.OutgoingMessage;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.UUID;

public interface OutgoingMessageDAO {
    @SqlUpdate("INSERT INTO OutgoingMessage (messageId, payload) " +
            "VALUES (:messageId, to_jsonb(:payload)::json) " +
            "ON CONFLICT (messageId) DO NOTHING")
    int insert(@Bind("messageId") UUID messageId,
               @Bind("payload") String payload);

    @SqlQuery("SELECT payload FROM OutgoingMessage WHERE messageId = :messageId")
    @RegisterColumnMapper(OutgoingMessageRSMapper.class)
    OutgoingMessage get(@Bind("messageId") UUID messageId);

    @SqlUpdate("DELETE FROM OutgoingMessage WHERE messageId = :messageId")
    void delete(@Bind("messageId") UUID messageId);
}
