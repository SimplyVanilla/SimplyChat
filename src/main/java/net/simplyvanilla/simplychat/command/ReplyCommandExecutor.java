package net.simplyvanilla.simplychat.command;

import net.kyori.adventure.text.Component;
import net.simplyvanilla.simplychat.SimplyChatPlugin;
import net.simplyvanilla.simplychat.state.PlayerState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class ReplyCommandExecutor implements CommandExecutor {

    private final SimplyChatPlugin plugin = SimplyChatPlugin.getInstance();
    private final MessageCommandExecutor messageCommandExecutor;

    public ReplyCommandExecutor(MessageCommandExecutor messageCommandExecutor) {
        this.messageCommandExecutor = messageCommandExecutor;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player sender)) {
            commandSender.sendMessage(Component.text("This command is only for players."));
            return true;
        }

        if (args.length < 1) {
            return false;
        }

        PlayerState playerState =
            plugin.getPlayerStateManager().getPlayerState(sender.getUniqueId());
        Optional<UUID> lastMessageSender = playerState.getLastMessageSender();
        if (lastMessageSender.isEmpty()) {
            sender.sendMessage(
                plugin.getColorCodeTranslatedConfigString("command.reply.noReceiverMessage"));
            return true;
        }

        Player receiver = plugin.getServer().getPlayer(lastMessageSender.get());
        if (receiver == null) {
            sender.sendMessage(plugin.getColorCodeTranslatedConfigString(
                "command.reply.receiverNotOnlineMessage"));
            return true;
        }

        String message = String.join(" ", args);
        messageCommandExecutor.message(sender, receiver, message);
        return true;
    }

}
