package net.simplyvanilla.simplychat.command;

import net.kyori.adventure.text.Component;
import net.simplyvanilla.simplychat.SimplyChatPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class MessageCommandExecutor implements CommandExecutor {

    private final SimplyChatPlugin plugin = SimplyChatPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("This command is only for players.");
            return false;
        }

        if (args.length < 2) {
            return false;
        }

        Player receiver = Bukkit.getPlayer(args[0]);

        if (receiver == null) {
            String message = plugin.getColorCodeTranslatedConfigString("command.message.receiverNotFoundMessage");
            message = message.replace("[receiver]", args[0]);
            sender.sendMessage(message);
            return true;
        }

        String message = Arrays.stream(args, 1, args.length)
                .collect(Collectors.joining(" "));

        message(sender, receiver, message);
        return true;
    }

    public void message(Player sender, Player receiver, String message) {
        String senderMessageFormat = plugin.getColorCodeTranslatedConfigString("command.message.senderMessageFormat");
        senderMessageFormat = PlaceholderAPI.setPlaceholders(sender, senderMessageFormat);
        String senderMessage = MessageFormat.expandInternalPlaceholders(sender.getName(), receiver.getName(), message, senderMessageFormat);
        sender.sendMessage(Component.text(senderMessage));

        String receiverMessageFormat = plugin.getColorCodeTranslatedConfigString("command.message.receiverMessageFormat");
        receiverMessageFormat = PlaceholderAPI.setPlaceholders(sender, receiverMessageFormat);
        String receiverMessage = MessageFormat.expandInternalPlaceholders(sender.getName(), receiver.getName(), message, receiverMessageFormat);
        receiver.sendMessage(Component.text(receiverMessage));

        plugin.getPlayerStateManager().getPlayerState(receiver.getUniqueId()).setLastMessageSender(sender.getUniqueId());
    }

}
