package net.simplyvanilla.simplychat.listener;

import io.github.miniplaceholders.api.MiniPlaceholders;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.simplyvanilla.simplychat.SimplyChatPlugin;
import net.simplyvanilla.simplychat.cooldown.ChatCooldownHandler;
import net.simplyvanilla.simplychat.database.Cache;
import net.simplyvanilla.simplychat.state.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final SimplyChatPlugin plugin = SimplyChatPlugin.getInstance();
    private final Cache cache = this.plugin.getCache();

    @EventHandler
    public void handlePlayerQuitEvent(PlayerQuitEvent event) {
        this.plugin.getPlayerStateManager().removeEntry(event.getPlayer().getUniqueId());
        this.cache.unloadPlayerIgnoreList(event.getPlayer());
    }

    @EventHandler
    public void handleAsyncChatEvent(AsyncChatEvent event) {
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        long currentMessageTime = System.currentTimeMillis();
        Player player = event.getPlayer();
        ChatCooldownHandler handler = this.plugin.getChatCooldownHandler();
        PlayerState playerState = SimplyChatPlugin.getInstance().getPlayerStateManager()
            .getPlayerState(player.getUniqueId());

        float remainingTime =
            handler.getRemainingTime(
                playerState,
                plainMessage
            );

        if (remainingTime > 0 && !player.hasPermission("simplychat.bypass.cooldown")) {
            event.setCancelled(true);
            player.sendMessage(this.plugin.getColorCodeTranslatedConfigString(
                "command.cooldown.playerIsInCooldownMessage",
                Formatter.number("time", remainingTime)));

            return;
        }

        // Mentioned from paper:
        // Listeners should be aware that modifying the list may throw UnsupportedOperationException if the event caller provides an unmodifiable set.
        try {
            event.viewers()
                .removeIf(viewer -> viewer instanceof Player viewerPlayer &&
                    this.cache.isPlayerIgnored(player, viewerPlayer));
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

        handler.setLastGlobalMessage(plainMessage, currentMessageTime);
        playerState.addLastMessage(plainMessage, currentMessageTime);
    }

    @EventHandler
    public void handlePlayerJoinEvent(PlayerJoinEvent event) {
        this.plugin.getCache().loadPlayerIgnoreList(event.getPlayer());
    }

    @EventHandler
    public void handlePlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();

        Player player = event.getPlayer();
        if (player.hasPermission("simplychat.bypass.cooldown")) {
            return;
        }
        ChatCooldownHandler handler = this.plugin.getChatCooldownHandler();
        PlayerState playerState = SimplyChatPlugin.getInstance().getPlayerStateManager()
            .getPlayerState(player.getUniqueId());

        if (this.isChattingCommand(message)) {
            // remove first character
            message = message.substring(1);
        }

        float remainingTime =
            handler.getRemainingTime(
                playerState,
                message
            );

        if (remainingTime > 0) {
            event.setCancelled(true);
            player.sendMessage(this.plugin.getColorCodeTranslatedConfigString(
                "command.cooldown.playerIsInCooldownCommand",
                Formatter.number("time", remainingTime)));
            return;
        }

        playerState.addLastMessage(message, System.currentTimeMillis());
    }

    private boolean isChattingCommand(String message) {
        String[] split = message.split(" ");
        if (split.length == 0) {
            return false;
        }
        String mainCommand = split[0];
        return this.plugin.getConfig().getStringList("cooldown.commands").stream()
            .anyMatch(command -> command.equalsIgnoreCase(mainCommand));
    }

}
