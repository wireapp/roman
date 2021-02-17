package com.wire.bots.roman.DAO.mappers;

import com.wire.xenon.tools.Logger;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UUIDMapper implements ColumnMapper<UUID> {
    @Override
    @Nullable
    public UUID map(ResultSet rs, int columnNumber, StatementContext ctx) {
        try {
            return getUuid(rs, "uuid");
        } catch (SQLException e) {
            Logger.error("UUIDMapper: i: %d, e: %s", columnNumber, e);
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
