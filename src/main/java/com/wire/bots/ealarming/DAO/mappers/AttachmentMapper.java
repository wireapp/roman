package com.wire.bots.ealarming.DAO.mappers;

import com.wire.bots.ealarming.model.Attachment;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AttachmentMapper implements ResultSetMapper<Attachment> {
    @Override
    @Nullable
    public Attachment map(int i, ResultSet rs, StatementContext statementContext) {
        Attachment attachment = new Attachment();
        try {
            attachment.id = rs.getInt("id");
            attachment.filename = rs.getString("filename");
            attachment.mimeType = rs.getString("mime_type");
            return attachment;
        } catch (SQLException e) {
            Logger.error("AttachmentMapper: i: %d, e: %s", i, e);
            return null;
        }
    }
}
