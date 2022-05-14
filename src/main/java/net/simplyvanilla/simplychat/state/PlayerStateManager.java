package net.simplyvanilla.simplychat.state;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerStateManager {

    private final Map<UUID, PlayerState> stateMap = new HashMap<>();

    public PlayerState getPlayerState(UUID uuid) {
        return this.stateMap.computeIfAbsent(uuid, k -> new PlayerState(null));
    }

    public void removeEntry(UUID uuid) {
        this.stateMap.remove(uuid);
    }

}
