package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.model.Alert;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AlertResultSetMapper implements ResultSetMapper<Alert> {
    @Override
    @Nullable
    public Alert map(int i, ResultSet rs, StatementContext statementContext) {
        Alert alert = new Alert();
        try {
            alert.id = rs.getInt("id");
            alert.created = rs.getString("created");
            alert.title = rs.getString("title");
            alert.message = rs.getString("message");
            alert.category = rs.getString("category");
            alert.severity = rs.getInt("severity");
            alert.creator = (UUID) rs.getObject("creator");
            Object contact = rs.getObject("contact");
            if (contact != null)
                alert.contact = (UUID) contact;
            alert.starting = rs.getString("starting");
            alert.ending = rs.getString("ending");
            alert.status = rs.getInt("status");
            alert.responses = rs.getString("responses");
            return alert;
        } catch (SQLException e) {
            Logger.error("AlertResultSetMapper: i: %d, e: %s", i, e);
            return null;
        }
    }
}