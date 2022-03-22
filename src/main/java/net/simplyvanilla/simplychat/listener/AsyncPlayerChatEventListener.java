package net.simplyvanilla.simplychat.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatEventListener implements Listener {

    private final String format;

    public AsyncPlayerChatEventListener(String format) {
        this.format = format;
    }

    @EventHandler
    public void handleAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String newFormat = PlaceholderAPI.setPlaceholders(player, format);
        event.setFormat(newFormat);
    }

}
