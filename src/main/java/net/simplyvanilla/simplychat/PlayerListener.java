package net.simplyvanilla.simplychat;

import io.github.miniplaceholders.api.MiniPlaceholders;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.simplyvanilla.simplychat.database.Cache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    public void handleAsyncChatEvent(AsyncChatEvent event) {
        // Mentioned from paper:
        // Listeners should be aware that modifying the list may throw UnsupportedOperationException if the event caller provides an unmodifiable set.
        try {
            event.viewers()
                .removeIf(viewer -> viewer instanceof Player player &&
                    cache.isPlayerIgnored(event.getPlayer(), player));
        } catch (UnsupportedOperationException ignored) {
            // This is thrown when the viewers are immutable.
            // This is fine, we just can't remove the ignored players.
        }

        event.renderer(
            (source, sourceDisplayName, message, viewer) -> MiniMessage.miniMessage()
                .deserialize(
                    this.plugin.getFormat(),
                    Placeholder.component("message", message),
                    MiniPlaceholders.getAudiencePlaceholders(source)
                ));
    }

    @EventHandler
    public void handlePlayerJoinEvent(PlayerJoinEvent event) {
        plugin.getCache().loadPlayerIgnoreList(event.getPlayer());
    }


}
