package net.simplyvanilla.simplychat.state;

import java.util.Optional;
import java.util.UUID;

public class PlayerState {

    private UUID lastMessageSender;

    public PlayerState(UUID lastMessageSender) {
        this.lastMessageSender = lastMessageSender;
    }

    public Optional<UUID> getLastMessageSender() {
        return Optional.ofNullable(lastMessageSender);
    }

    public void setLastMessageSender(UUID uuid) {
        this.lastMessageSender = uuid;
    }

}
