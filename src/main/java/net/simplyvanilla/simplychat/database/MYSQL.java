package net.simplyvanilla.simplychat.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.simplyvanilla.simplychat.SimplyChatPlugin;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class MYSQL {

    SimplyChatPlugin plugin = SimplyChatPlugin.getInstance();
    String tableName = plugin.getConfig().getString("database.ignoreDataTableName");
    String fieldKey = plugin.getConfig().getString("database.uuidFieldName");
    String fieldValue = plugin.getConfig().getString("database.uuidValueName");

    Connection connection;
    Statement statement;

    public synchronized void connect() {
        try {
            plugin.getLogger().log(Level.INFO, "Connecting to MYSQL server, please wait...");
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                plugin.getConfig().getString("database.url"),
                plugin.getConfig().getString("database.username"),
                plugin.getConfig().getString("database.password"));

            statement = connection.createStatement();

            // See here https://stackoverflow.com/a/1208477/19627655
            String tableCheckQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + fieldKey + " MEDIUMTEXT, " + fieldValue + " MEDIUMTEXT);";
            statement.executeUpdate(tableCheckQuery);

            plugin.getLogger().log(Level.INFO, "Connected to the MYSQL server!");
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        if (connection == null || statement == null) {
            plugin.getLogger().log(Level.SEVERE, "Database connection is not stable. Plugin disabling...");
            plugin.getLogger().log(Level.SEVERE, "Please check your database. And config file.");
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    public String[] getPlayerIgnoreData(String playerUUID) {
        try {
            String playerSearchQuery = "SELECT * FROM " + tableName + " WHERE " + fieldKey + " =?";
            PreparedStatement playerSearchQueryPS = connection.prepareStatement(playerSearchQuery);
            playerSearchQueryPS.setString(1, playerUUID);

            List<String> ignoredPlayerUUIDs = new ArrayList<>();

            ResultSet rs = playerSearchQueryPS.executeQuery();
            if (rs.next()) {
                JsonArray ignoreJSON = JsonParser.parseString(rs.getString(fieldValue)).getAsJsonArray();
                for (JsonElement jsonElement : ignoreJSON) {
                    ignoredPlayerUUIDs.add(jsonElement.getAsString());
                }
                return ignoredPlayerUUIDs.toArray(new String[0]);
            } else {
                String insertNewPlayerQuery = "INSERT INTO " + tableName + "(" + fieldKey + "," + fieldValue + ") VALUES(?, ?);";
                PreparedStatement insertNewPlayerQueryPS = connection.prepareStatement(insertNewPlayerQuery);
                insertNewPlayerQueryPS.setString(1, playerUUID);
                insertNewPlayerQueryPS.setString(2, "[]");
                insertNewPlayerQueryPS.executeUpdate();
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Database connection is not stable. Plugin disabling...");
            plugin.getLogger().log(Level.SEVERE, "Please check your database.");
            plugin.getPluginLoader().disablePlugin(plugin);
            ex.printStackTrace();
        }
        return new String[]{};
    }

    public void updatePlayerIgnoreData(Player ignorer, List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append(" ");
        }
        try {
            String playerListUpdateQuery = "UPDATE " + tableName + " SET " + fieldValue + " = ? " + " WHERE " + fieldKey + " = ?";
            PreparedStatement playerListUpdateQueryPS = connection.prepareStatement(playerListUpdateQuery);
            playerListUpdateQueryPS.setString(1, JSONArray.toJSONString(list));
            playerListUpdateQueryPS.setString(2, ignorer.getUniqueId().toString());
            playerListUpdateQueryPS.executeUpdate();
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Database connection is not stable. Plugin disabling...");
            plugin.getLogger().log(Level.SEVERE, "Please check your database.");
            plugin.getPluginLoader().disablePlugin(plugin);
            ex.printStackTrace();
        }
    }

    public void addIgnoredPlayer(Player ignorer, Player ignored) {
        List<String> ignoredPlayers = plugin.getCache().getPlayerIgnoreInfo(ignorer);
        ignoredPlayers.add(ignored.getUniqueId().toString());
        updatePlayerIgnoreData(ignorer, ignoredPlayers);
    }

    public void removeIgnoredPlayer(Player ignorer, Player ignored) {
        List<String> ignoredPlayers = plugin.getCache().getPlayerIgnoreInfo(ignorer);
        ignoredPlayers.remove(ignored.getUniqueId().toString());
        updatePlayerIgnoreData(ignorer, ignoredPlayers);
    }

}
