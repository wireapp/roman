package com.wire.bots.ealarming;

import com.wire.bots.ealarming.model.Config;
import com.wire.bots.sdk.Configuration;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class Database {
    private final Config conf;

    Database(Config conf) {
        this.conf = conf;
    }

    boolean insertSubscriber(UUID botId, UUID convId) throws Exception {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("INSERT INTO Alert (botId, conversationId, serviceId) VALUES (?, ?, ?)");
            stmt.setObject(1, botId);
            stmt.setObject(2, convId);
            return stmt.executeUpdate() == 1;
        }
    }

    ArrayList<UUID> getSubscribers() throws Exception {
        ArrayList<UUID> ret = new ArrayList<>();
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT botId FROM Alert");
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                ret.add((UUID) resultSet.getObject("botId"));
            }
        }
        return ret;
    }

    ArrayList<UUID> getMySubscribers() throws Exception {
        ArrayList<UUID> ret = new ArrayList<>();
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT botId FROM Alert WHERE serviceId = ?");
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                ret.add((UUID) resultSet.getObject("botId"));
            }
        }
        return ret;
    }

    boolean unsubscribe(UUID botId) throws SQLException {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("DELETE FROM Alert WHERE botId = ?");
            stmt.setObject(1, botId);
            return stmt.executeUpdate() == 1;
        }
    }

    UUID getConversationId(UUID botId) throws Exception {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT conversationId FROM Alert WHERE botId = ?");
            stmt.setObject(1, botId);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return (UUID) resultSet.getObject("conversationId");
            }
        }
        return null;
    }

    boolean insertAnnotation(UUID botId, String key, String value) throws SQLException {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("INSERT INTO Annotations (botId, key, value) VALUES (?, ?, ?)");
            stmt.setObject(1, botId);
            stmt.setString(2, key);
            stmt.setString(3, value);
            return stmt.executeUpdate() == 1;
        }
    }

    Map<String, String> getAnnotations(UUID botId) throws Exception {
        HashMap<String, String> ret = new HashMap<>();
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("SELECT key, value FROM Annotations WHERE botId = ?");
            stmt.setObject(1, botId);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                String value = resultSet.getString("value");
                String key = resultSet.getString("key");
                ret.put(key, value);
            }
        }
        return ret;
    }

    private Connection newConnection() throws SQLException {
        Configuration.DB postgres = conf.getDB();
        String url = String.format("jdbc:%s://%s:%d/%s", postgres.driver, postgres.host, postgres.port, postgres.database);
        return DriverManager.getConnection(url, postgres.user, postgres.password);
    }

    boolean removeAnnotation(UUID botId, String key, String value) throws SQLException {
        try (Connection c = newConnection()) {
            PreparedStatement stmt = c.prepareStatement("DELETE FROM Annotations WHERE botId = ? AND key = ? AND value = ?");
            stmt.setObject(1, botId);
            stmt.setString(2, key);
            stmt.setString(3, value);
            return stmt.executeUpdate() == 1;
        }
    }
}
