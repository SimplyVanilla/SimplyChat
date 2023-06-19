package net.simplyvanilla.simplychat.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.simplyvanilla.simplychat.SimplyChatPlugin;
import net.simplyvanilla.simplychat.database.Cache;
import net.simplyvanilla.simplychat.database.MYSQL;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class IgnoreCommandExecutor implements CommandExecutor {

    public static final String PLAYER_NAME = "player_name";
    SimplyChatPlugin plugin = SimplyChatPlugin.getInstance();
    Cache cache = plugin.getCache();
    MYSQL database = plugin.getDatabase();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (handleMultipleArguments(sender, command, args)) {
            return true;
        }

        if (!command.getName().equals("ignorelist")) {
            return false;
        }
        List<String> ignoredPlayers = cache.getPlayerIgnoreInfo((Player) sender);
        if (ignoredPlayers.isEmpty() || ignoredPlayers.get(0).length() == 0) {
            sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                "command.ignore.notIgnoredAnyPlayerMessage"));
            return true;
        }
        Component component = Component.empty();
        for (String ignoredPlayer : ignoredPlayers) {
            OfflinePlayer offlinePlayer =
                Bukkit.getOfflinePlayer(UUID.fromString(ignoredPlayer));
            component = component.append(plugin.getColorCodeTranslatedConfigString(
                "command.ignore.ignoredPlayerDisplayMessage",
                Placeholder.unparsed(PLAYER_NAME, offlinePlayer.getName())));
        }
        sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
            "command.ignore.ignoredPlayerListMessage").append(component));
        return true;
    }

    private boolean handleMultipleArguments(@NotNull CommandSender sender, @NotNull Command command,
                                            @NotNull String[] args) {
        if (args.length == 1) {
            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                    "command.ignore.playerCannotFoundMessage",
                    Placeholder.unparsed(PLAYER_NAME, args[0])));
                return true;
            }

            if (targetPlayer == sender) {
                return true;
            }

            if (command.getName().equals("ignore")) {
                if (cache.getPlayerIgnoreInfo((Player) sender)
                    .contains(targetPlayer.getUniqueId().toString())) {
                    sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                        "command.ignore.playerAlreadyIgnoredMessage",
                        Placeholder.unparsed(PLAYER_NAME, args[0])));
                    return true;
                }
                database.addIgnoredPlayer((Player) sender, targetPlayer);
                sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                    "command.ignore.playerIgnoredMessage",
                    Placeholder.unparsed(PLAYER_NAME, args[0])));
            }

            if (command.getName().equals("unignore")) {
                if (!cache.getPlayerIgnoreInfo((Player) sender)
                    .contains(targetPlayer.getUniqueId().toString())) {
                    sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                        "command.ignore.playerIsNotIgnoredMessage",
                        Placeholder.unparsed(PLAYER_NAME, args[0])));
                    return true;
                }
                database.removeIgnoredPlayer((Player) sender, targetPlayer);
                sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                    "command.ignore.playerUnignoredMessage",
                    Placeholder.unparsed(PLAYER_NAME, args[0])));
            }
            return true;
        }
        return false;
    }
}
