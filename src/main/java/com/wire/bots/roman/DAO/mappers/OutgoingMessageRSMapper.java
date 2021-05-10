package com.wire.bots.roman.DAO.mappers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.model.OutgoingMessage;
import com.wire.xenon.tools.Logger;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OutgoingMessageRSMapper implements ColumnMapper<OutgoingMessage> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public OutgoingMessage map(ResultSet rs, int columnNumber, StatementContext ctx) throws SQLException {
        OutgoingMessage message = null;
        try {
            final String payload = getPayload(rs);
            message = mapper.readValue(payload, OutgoingMessage.class);
        } catch (IOException e) {
            Logger.exception("getPayload", e);
        }

        return message;
    }

    private String getPayload(ResultSet rs) throws SQLException, IOException {
        JsonParser jsonParser = mapper.getFactory().createParser(rs.getString("payload"));
        final TreeNode treeNode = jsonParser.readValueAsTree();
        JsonNode node = (JsonNode) treeNode;
        return node.asText();
    }
}
