package com.wire.bots.roman.DAO.mappers;

import com.wire.bots.roman.model.ExternalService;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BotsMapper implements ResultSetMapper<ExternalService> {
    @Override
    @Nullable
    public ExternalService map(int i, ResultSet rs, StatementContext statementContext) {
        ExternalService ret = new ExternalService();
        try {
            ret.botId = getUuid(rs, "id");
            ret.url = rs.getString("url");
            ret.auth = rs.getString("token");

            return ret;
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
