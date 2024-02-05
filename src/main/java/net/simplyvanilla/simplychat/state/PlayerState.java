package net.simplyvanilla.simplychat.state;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.UUID;

public class PlayerState {
    private static final int MAX_HISTORY = 3;

    private UUID lastMessageSender;
    private Deque<String> lastMessages;
    private long lastMessageTime;

    public PlayerState(UUID lastMessageSender) {
        this.lastMessageSender = lastMessageSender;
        this.lastMessages = new ArrayDeque<>(MAX_HISTORY);
        this.lastMessageTime = 0;
    }

    public Optional<UUID> getLastMessageSender() {
        return Optional.ofNullable(this.lastMessageSender);
    }

    public void setLastMessageSender(UUID uuid) {
        this.lastMessageSender = uuid;
    }

    public void addLastMessage(String message, long time) {
        if (this.lastMessages.size() >= MAX_HISTORY) {
            this.lastMessages.removeLast();
        }

        this.lastMessages.offerFirst(removeDigits(message));
        this.lastMessageTime = time;
    }

    public boolean isLastMessage(String message) {
        // Check if message is null
        if (message != null) {
            // Remove digits from message
            message = removeDigits(message);

            // Get the length of the message
            int length = message.length();

            // Iterate over last messages
            for (String lastMessage : lastMessages) {
                // Check if equals the last message
                if (message.equals(lastMessage)) {
                    return true;
                }
                // Check if equals last message length
                if (length > 16 && length == lastMessage.length()) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String removeDigits(String str) {
        // Converting the given string
        // into a character array
        char[] charArray = str.toCharArray();
        String result = "";

        // Traverse the character array
        for (int i = 0; i < charArray.length; i++) {
            // Check if the specified character is not digit
            // then add this character into result variable
            if (!Character.isDigit(charArray[i])) {
                result = result + charArray[i];
            }
        }

        return result;
    }

    public long getLastMessageTime() {
        return this.lastMessageTime;
    }
}
