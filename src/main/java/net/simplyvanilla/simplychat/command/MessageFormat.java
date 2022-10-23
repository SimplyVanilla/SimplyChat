package net.simplyvanilla.simplychat.command;

public final class MessageFormat {

    private MessageFormat() {}

    public static String expandInternalPlaceholders(String senderName, String receiverName, String message, String format) {
        return format.replace("[sender]", senderName)
                .replace("[receiver]", receiverName)
                .replace("[message]", message);
    }

    public static String expandInternalPlaceholders(String replacer,String player, String format) {
        return format.replace(replacer, player);
    }


}
