package net.simplyvanilla.command;

public final class MessageFormat {

    private MessageFormat() {
    }

    public static String expandInternalPlaceholders(String senderName, String receiverName, String message, String format) {
        return format.replaceAll("\\[sender]", senderName)
                .replaceAll("\\[receiver]", receiverName)
                .replaceAll("\\[message]", message);
    }

}
