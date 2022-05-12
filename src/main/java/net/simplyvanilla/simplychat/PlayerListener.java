package net.simplyvanilla.simplychat;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final SimplyChatPlugin plugin = SimplyChatPlugin.getInstance();

    @EventHandler
    public void handlePlayerQuitEvent(PlayerQuitEvent event) {
        plugin.getPlayerStateManager().removeEntry(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void handleAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String newFormat = PlaceholderAPI.setPlaceholders(player, plugin.getFormat());
        event.setFormat(newFormat);
    }

}
