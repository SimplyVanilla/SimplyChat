package net.simplyvanilla.simplychat.database;

import org.bukkit.entity.Player;

import java.util.*;

public class Cache {

    private final Map<Player, List<String>> playerIgnoreCache = new HashMap<>();
    private final MYSQL database;

    public Cache(MYSQL database) {
        this.database = database;
    }

    public void loadPlayerIgnoreList(Player player) {
        String[] playerNames = database.getPlayerIgnoreData(player.getUniqueId().toString());
        if (playerNames.length != 0)
            playerIgnoreCache.put(player, new ArrayList<>(Arrays.asList(playerNames)));
        else
            playerIgnoreCache.put(player, new ArrayList<>());
    }

    public void unloadPlayerIgnoreList(Player player) {
        playerIgnoreCache.remove(player);
    }

    public List<String> getPlayerIgnoreInfo(Player player) {
        return playerIgnoreCache.getOrDefault(player, new ArrayList<>());
    }

    public boolean isPlayerIgnored(Player messageSender, Player messageReceiver) {
        return getPlayerIgnoreInfo(messageReceiver).contains(messageSender.getUniqueId().toString());
    }
}
