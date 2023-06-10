package net.simplyvanilla.simplychat.command;

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

    SimplyChatPlugin plugin = SimplyChatPlugin.getInstance();
    Cache cache = plugin.getCache();
    MYSQL database = plugin.getDatabase();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                    "command.ignore.playerCannotFoundMessage").replace("[player_name]",
                    args[0]));
                return true;
            } else if (targetPlayer == sender) {
                return true;
            }

            if (command.getName().equals("ignore")) {
                if (cache.getPlayerIgnoreInfo((Player) sender)
                    .contains(targetPlayer.getUniqueId().toString())) {
                    sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                        "command.ignore.playerAlreadyIgnoredMessage").replace("[player_name]",
                        args[0]));
                    return true;
                }
                database.addIgnoredPlayer((Player) sender, targetPlayer);
                sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                    "command.ignore.playerIgnoredMessage").replace("[player_name]",
                    args[0]));
            } else if (command.getName().equals("unignore")) {
                if (!cache.getPlayerIgnoreInfo((Player) sender)
                    .contains(targetPlayer.getUniqueId().toString())) {
                    sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                        "command.ignore.playerIsNotIgnoredMessage").replace("[player_name]",
                        args[0]));
                    return true;
                }
                database.removeIgnoredPlayer((Player) sender, targetPlayer);
                sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                    "command.ignore.playerUnignoredMessage").replace("[player_name]",
                    args[0]));
            }
            return true;
        }

        if (command.getName().equals("ignorelist")) {
            List<String> ignoredPlayers = cache.getPlayerIgnoreInfo((Player) sender);
            if (ignoredPlayers.isEmpty() || ignoredPlayers.get(0).length() == 0) {
                sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                    "command.ignore.notIgnoredAnyPlayerMessage"));
                return true;
            }
            StringBuilder sb = new StringBuilder();
            for (String ignoredPlayer : ignoredPlayers) {
                OfflinePlayer offlinePlayer =
                    Bukkit.getOfflinePlayer(UUID.fromString(ignoredPlayer));
                String newFormat =
                    plugin.getColorCodeTranslatedConfigString(
                        "command.ignore.ignoredPlayerDisplayMessage").replace("[player_name]",
                        offlinePlayer.getName());
                sb.append(newFormat);
            }
            sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                "command.ignore.ignoredPlayerListMessage") + sb.toString());
            return true;
        }
        return true;
    }
}
