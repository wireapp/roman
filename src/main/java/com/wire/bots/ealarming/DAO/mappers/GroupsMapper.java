package com.wire.bots.ealarming.DAO.mappers;

import com.wire.bots.ealarming.model.Group;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupsMapper implements ResultSetMapper<Group> {
    @Override
    @Nullable
    public Group map(int i, ResultSet rs, StatementContext statementContext) {
        Group ret = new Group();
        try {
            ret.id = rs.getInt("id");
            ret.name = rs.getString("name");
            ret.type = rs.getInt("type");
            ret.deleted = rs.getInt("deleted");
            return ret;
        } catch (SQLException e) {
            Logger.error("GroupMapper: i: %d, e: %s", i, e);
            return null;
        }
    }
}
