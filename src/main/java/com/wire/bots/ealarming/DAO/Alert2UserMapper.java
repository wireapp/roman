package com.wire.bots.ealarming.DAO;

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
            ret.userId = (UUID) rs.getObject("user_id");
            ret.messageStatus = rs.getInt("message_status");
            ret.escalated = rs.getString("escalated");
            ret.responseId = rs.getInt("response_id");
            return ret;
        } catch (SQLException e) {
            Logger.error("Alert2UserMapper: i: %d, e: %s", i, e);
            return null;
        }
    }
}
