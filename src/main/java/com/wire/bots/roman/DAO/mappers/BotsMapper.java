package com.wire.bots.roman.DAO.mappers;

import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BotsMapper implements ResultSetMapper<UUID> {
    @Override
    @Nullable
    public UUID map(int i, ResultSet rs, StatementContext statementContext) {
        try {
            return getUuid(rs, "uuid");
        } catch (SQLException e) {
            Logger.error("BotsMapper: i: %d, e: %s", i, e);
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
