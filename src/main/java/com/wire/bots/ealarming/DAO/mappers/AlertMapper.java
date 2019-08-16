package com.wire.bots.ealarming.DAO.mappers;

import com.wire.bots.ealarming.model.Alert;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AlertMapper implements ResultSetMapper<Alert> {
    @Override
    @Nullable
    public Alert map(int i, ResultSet rs, StatementContext statementContext) {
        Alert alert = new Alert();
        try {
            alert.id = rs.getInt("id");
            alert.created = rs.getString("created");
            alert.title = rs.getString("title");
            alert.message = rs.getString("message");
            alert.severity = rs.getInt("severity");
            alert.creator = getUuid(rs, "creator");
            alert.contact = getUuid(rs, "contact");
            alert.starting = rs.getString("starting");
            alert.ending = rs.getString("ending");
            alert.status = rs.getInt("status");
            return alert;
        } catch (SQLException e) {
            Logger.error("AlertResultSetMapper: i: %d, e: %s", i, e);
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
