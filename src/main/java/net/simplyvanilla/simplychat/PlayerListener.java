package net.simplyvanilla.simplychat;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.simplyvanilla.simplychat.database.Cache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

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
        event.getRecipients()
            .removeIf(recipient -> cache.isPlayerIgnored(event.getPlayer(), recipient));
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
                    this.papiTag(source),
                    Placeholder.component("message", message)
                ));
    }

    @EventHandler
    public void handlePlayerJoinEvent(PlayerJoinEvent event) {
        plugin.getCache().loadPlayerIgnoreList(event.getPlayer());
    }

    /**
     * Creates a tag resolver capable of resolving PlaceholderAPI tags for a given player.
     *
     * @param player the player
     * @return the tag resolver
     */
    public @NotNull TagResolver papiTag(final @NotNull Player player) {
        return TagResolver.resolver("papi", (argumentQueue, context) -> {
            // Get the string placeholder that they want to use.
            final String papiPlaceholder =
                argumentQueue.popOr("papi tag requires an argument").value();

            // Then get PAPI to parse the placeholder for the given player.
            final String parsedPlaceholder =
                PlaceholderAPI.setPlaceholders(player, '%' + papiPlaceholder + '%');

            // We need to turn this ugly legacy string into a nice component.
            final Component componentPlaceholder =
                LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder);

            // Finally, return the tag instance to insert the placeholder!
            return Tag.selfClosingInserting(componentPlaceholder);
        });
    }


}
