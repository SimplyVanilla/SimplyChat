package net.simplyvanilla.simplychat.command;

import java.util.regex.Matcher;

public final class MessageFormat {

    private MessageFormat() {}

    public static String expandInternalPlaceholders(String senderName, String receiverName, String message, String format) {
        return format.replace("[sender]", senderName)
                .replace("[receiver]", receiverName)
                .replace("[message]", message);
    }

}
