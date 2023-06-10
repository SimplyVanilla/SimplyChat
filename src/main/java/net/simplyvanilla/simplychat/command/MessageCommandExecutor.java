package net.simplyvanilla.simplychat.command;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

import java.util.Arrays;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.simplyvanilla.simplychat.SimplyChatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MessageCommandExecutor implements CommandExecutor {

    private final SimplyChatPlugin plugin = SimplyChatPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage("This command is only for players.");
            return false;
        }

        if (args.length < 2) {
            return false;
        }

        Player receiver = Bukkit.getPlayer(args[0]);

        if (receiver == null) {
            String message = plugin.getColorCodeTranslatedConfigString(
                "command.message.receiverNotFoundMessage");
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
        // check if receiving player is ignoring sender
        boolean receiverIgnoredSender = plugin.getCache().isPlayerIgnored(receiver, sender);

        if (receiverIgnoredSender) {
            sender.sendMessage(
                plugin.getColorCodeTranslatedConfigString(
                        "command.message.senderIgnoreErrorMessage")
                    .replace("[receiver_name]", receiver.getName()));
            return;
        }

        String senderMessageFormat =
            plugin.getColorCodeTranslatedConfigString("command.message.senderMessageFormat");

        sender.sendMessage(miniMessage().deserialize(senderMessageFormat,
            Placeholder.component("sender", sender.displayName()),
            Placeholder.component("receiver", receiver.displayName()),
            Placeholder.component("message", Component.text(message)))
        );

        String receiverMessageFormat =
            plugin.getColorCodeTranslatedConfigString("command.message.receiverMessageFormat");

        sender.sendMessage(miniMessage().deserialize(receiverMessageFormat,
            Placeholder.component("sender", sender.displayName()),
            Placeholder.component("receiver", receiver.displayName()),
            Placeholder.component("message", Component.text(message)))
        );

        plugin.getPlayerStateManager().getPlayerState(receiver.getUniqueId())
            .setLastMessageSender(sender.getUniqueId());
    }

}
