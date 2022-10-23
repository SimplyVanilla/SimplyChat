package net.simplyvanilla.simplychat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.simplyvanilla.simplychat.database.Cache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final SimplyChatPlugin plugin = SimplyChatPlugin.getInstance();
    private final Cache cache = plugin.getCache();

    @EventHandler
    public void handlePlayerQuitEvent(PlayerQuitEvent event) {
        plugin.getPlayerStateManager().removeEntry(event.getPlayer().getUniqueId());
        cache.unloadPlayerIgnoreList(event.getPlayer());
    }

    @EventHandler
    public void handleAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String newFormat = PlaceholderAPI.setPlaceholders(player, plugin.getFormat());
        event.setFormat(newFormat);
        event.getRecipients().removeIf(recipient -> cache.isPlayerIgnored(event.getPlayer(), recipient));
    }

    @EventHandler
    public void handlePlayerJoinEvent(PlayerJoinEvent event) {
        plugin.getCache().loadPlayerIgnoreList(event.getPlayer());
    }


}
