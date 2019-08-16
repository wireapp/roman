package com.wire.bots.ealarming.DAO.mappers;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportMapper implements ResultSetMapper<Alert2UserDAO._Pair> {
    @Override
    @Nullable
    public Alert2UserDAO._Pair map(int i, ResultSet rs, StatementContext statementContext) {
        Alert2UserDAO._Pair ret = new Alert2UserDAO._Pair();
        try {
            ret.type = rs.getInt("message_status");
            ret.count = rs.getInt("count");
            return ret;
        } catch (SQLException e) {
            Logger.error("ReportMapper: i: %d, e: %s", i, e);
            return null;
        }
    }
}
