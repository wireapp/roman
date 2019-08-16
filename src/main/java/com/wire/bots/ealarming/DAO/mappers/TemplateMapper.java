package com.wire.bots.ealarming.DAO.mappers;

import com.wire.bots.ealarming.model.Template;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TemplateMapper implements ResultSetMapper<Template> {
    @Override
    @Nullable
    public Template map(int i, ResultSet rs, StatementContext statementContext) {
        Template template = new Template();
        try {
            template.id = rs.getInt("id");
            template.created = rs.getString("created");
            template.title = rs.getString("title");
            template.message = rs.getString("message");
            Object contact = rs.getObject("contact");
            if (contact != null)
                template.contact = (UUID) contact;
            template.severity = rs.getInt("severity");
            return template;
        } catch (SQLException e) {
            Logger.error("TemplateMapper: i: %d, e: %s", i, e);
            return null;
        }
    }
}
