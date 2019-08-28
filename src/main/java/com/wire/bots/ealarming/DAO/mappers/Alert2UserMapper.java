package com.wire.bots.ealarming.DAO.mappers;

import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Alert2UserMapper implements ResultSetMapper<Alert2User> {
    @Override
    @Nullable
    public Alert2User map(int i, ResultSet rs, StatementContext statementContext) {
        Alert2User ret = new Alert2User();
        try {
            ret.alertId = rs.getInt("alert_id");
            ret.userId = getUuid(rs, "user_id");
            ret.status = Alert2User.Type.values()[rs.getInt("message_status")];
            ret.messageId = getUuid(rs, "message_id");
            ret.escalated = rs.getInt("escalated") != 0 ? rs.getInt("escalated") : null;
            ret.response = rs.getString("response");
            ret.created = rs.getString("created");
            return ret;
        } catch (SQLException e) {
            Logger.error("Alert2UserMapper: i: %d, e: %s", i, e);
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
