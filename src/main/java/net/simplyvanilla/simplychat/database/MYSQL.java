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
    String tableName = plugin.getConfig().getString("database.ignoreTableName");
    Connection connection;
    Statement statement;

    public synchronized void connect() {
        try {
            plugin.getLogger().log(Level.INFO, "Connecting to MYSQL server, please wait...");
            connection = DriverManager.getConnection(
                plugin.getConfig().getString("database.url"),
                plugin.getConfig().getString("database.username"),
                plugin.getConfig().getString("database.password"));

            statement = connection.createStatement();

            // See here https://stackoverflow.com/a/1208477/19627655
            String tableCheckQuery = String.format(
                """
                        CREATE TABLE IF NOT EXISTS `%s` (
                            `id` int unsigned NOT NULL AUTO_INCREMENT,
                            `uuid` char(36) NOT NULL,
                            `data` text NOT NULL,
                            `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uuid` (`uuid`)
                        )
                    """,
                tableName
            );
            statement.executeUpdate(tableCheckQuery);

            plugin.getLogger().log(Level.INFO, "Connected to the MYSQL server!");
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        if (connection == null || statement == null) {
            plugin.getLogger()
                .log(Level.SEVERE, "Database connection is not stable. Plugin disabling...");
            plugin.getLogger().log(Level.SEVERE, "Please check your database. And config file.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public String[] getPlayerIgnoreData(String playerUUID) {

        List<String> ignoredPlayerUUIDs = new ArrayList<>();
        String playerSearchQuery = String.format("SELECT * FROM `%s` WHERE `uuid` =?", tableName);

        try (
            PreparedStatement playerSearchQueryPS = connection.prepareStatement(
                playerSearchQuery)) {
            playerSearchQueryPS.setString(1, playerUUID);

            try (ResultSet rs = playerSearchQueryPS.executeQuery()) {
                if (rs.next()) {
                    JsonArray ignoreJSON =
                        JsonParser.parseString(rs.getString("data")).getAsJsonArray();
                    for (JsonElement jsonElement : ignoreJSON) {
                        ignoredPlayerUUIDs.add(jsonElement.getAsString());
                    }
                    return ignoredPlayerUUIDs.toArray(new String[0]);
                }
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to getPlayerIgnoreData...");
            ex.printStackTrace();
        }

        return new String[]{};
    }

    public void updatePlayerIgnoreData(Player ignorer, List<String> list) {
        String playerListUpdateQuery = String.format(
            """
                    INSERT INTO `%s` (`uuid`, `data`) VALUES (?, ?) AS `new`
                    ON DUPLICATE KEY UPDATE `data` = `new`.`data`, `updated_at` = CURRENT_TIMESTAMP
                """,
            tableName
        );

        try (PreparedStatement playerListUpdateQueryPS = connection.prepareStatement(
            playerListUpdateQuery)) {
            playerListUpdateQueryPS.setString(1, ignorer.getUniqueId().toString());
            playerListUpdateQueryPS.setString(2,
                JSONArray.toJSONString(list.subList(0, Math.min(list.size(), 100))));
            playerListUpdateQueryPS.executeUpdate();
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to updatePlayerIgnoreData...");
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
