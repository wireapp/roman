package com.wire.bots.ealarming.DAO;

import com.wire.bots.ealarming.model.User;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserMapper implements ResultSetMapper<User> {
    @Override
    @Nullable
    public User map(int i, ResultSet rs, StatementContext statementContext) {
        User user = new User();
        try {
            user.id = rs.getInt("id");
            user.firstname = rs.getString("firstname");
            user.title = rs.getString("title");
            user.surname = rs.getString("surname");
            user.department = rs.getString("department");
            user.location = rs.getString("location");
            Object userId = rs.getObject("user_id");
            if (userId != null)
                user.userId = (UUID) userId;

            return user;
        } catch (SQLException e) {
            Logger.error("AlertResultSetMapper: i: %d, e: %s", i, e);
            return null;
        }
    }
}
