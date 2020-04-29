package com.wire.bots.roman.DAO.mappers;

import com.wire.bots.roman.model.Provider;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ProviderMapper implements ResultSetMapper<Provider> {
    @Override
    public Provider map(int i, ResultSet rs, StatementContext statementContext) throws SQLException {
        Provider ret = new Provider();
        ret.id = getUuid(rs, "id");
        ret.email = rs.getString("email");
        ret.password = rs.getString("password");
        ret.hash = rs.getString("hash");
        ret.serviceAuth = rs.getString("service_auth");
        ret.serviceUrl = rs.getString("url");
        ret.serviceId = getUuid(rs, "service");
        ret.name = rs.getString("name");
        ret.serviceName = rs.getString("service_name");

        return ret;
    }

    private UUID getUuid(ResultSet rs, String name) throws SQLException {
        UUID contact = null;
        Object rsObject = rs.getObject(name);
        if (rsObject != null)
            contact = (UUID) rsObject;
        return contact;
    }
}
