package com.wire.bots.roman.DAO.mappers;

import com.wire.bots.roman.model.Provider;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ProviderMapper implements ResultSetMapper<Provider> {
    @Override
    @Nullable
    public Provider map(int i, ResultSet rs, StatementContext statementContext) {
        Provider ret = new Provider();
        try {
            ret.id = getUuid(rs, "id");
            ret.email = rs.getString("email");
            ret.password = rs.getString("password");
            ret.hash = rs.getString("hash");
            ret.serviceAuth = rs.getString("service_auth");
            ret.serviceUrl = rs.getString("url");

            return ret;
        } catch (SQLException e) {
            Logger.error("ProviderMapper: i: %d, e: %s", i, e);
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
