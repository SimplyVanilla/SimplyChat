package net.simplyvanilla.simplychat.cooldown;

import net.simplyvanilla.simplychat.state.PlayerState;
import org.bukkit.configuration.ConfigurationSection;

public class ChatCooldownHandler {
    private final int repeatTimeGlobal;
    private final int repeatTime;
    private final int normalTime;
    private final int commandTime;
    private long lastGlobalMessageTime = 0L;
    private String lastGlobalMessage = "";

    public ChatCooldownHandler(ConfigurationSection section) {
        this.repeatTimeGlobal = section.getInt("repeat-time-global");
        this.repeatTime = section.getInt("repeat-time");
        this.normalTime = section.getInt("normal-time");
        this.commandTime = section.getInt("command-time");
    }

    // Reference: https://github.com/arkflame/ChatSentinel/blob/cb2cbb81f504a147363484b9636d9c0867f1c81e/src/main/java/dev/_2lstudios/chatsentinel/shared/modules/CooldownModule.java#L22C2-L45C3
    public float getRemainingTime(PlayerState playerState, String message) {
        long currentTime = System.currentTimeMillis();
        long lastMessageTime = currentTime - playerState.getLastMessageTime();
        long lastMessageTimeGlobal = currentTime - this.lastGlobalMessageTime;
        long remainingTime;

        if (message.startsWith("/")) {
            remainingTime = this.commandTime - lastMessageTime;
        } else if (playerState.isLastMessage(message) && lastMessageTime < this.repeatTime) {
            remainingTime = this.repeatTime - lastMessageTime;
        } else if (this.lastGlobalMessage.equals(message) &&
            lastMessageTimeGlobal < this.repeatTimeGlobal) {
            remainingTime = this.repeatTimeGlobal - lastMessageTimeGlobal;
        } else {
            remainingTime = this.normalTime - lastMessageTime;
        }

        if (remainingTime > 0) {
            return ((float) (remainingTime / 100)) / 10;
        }

        return 0;
    }

    public void setLastGlobalMessage(String lastMessage, long lastMessageTime) {
        this.lastGlobalMessage = lastMessage;
        this.lastGlobalMessageTime = lastMessageTime;
    }

}
