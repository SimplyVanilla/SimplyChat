package net.simplyvanilla.simplychat.command;

import java.util.regex.Matcher;

public final class MessageFormat {

    private MessageFormat() {}

    public static String expandInternalPlaceholders(String senderName, String receiverName, String message, String format) {
        return format.replaceAll("\\[sender]", senderName)
                .replaceAll("\\[receiver]", receiverName)
                .replaceAll("\\[message]", Matcher.quoteReplacement(message));
    }

}
