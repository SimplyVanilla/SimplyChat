package net.simplyvanilla.state;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerStateManager {

    private final Map<UUID, PlayerState> stateMap = new HashMap<>();

    public PlayerState getPlayerState(UUID uuid) {
        return stateMap.computeIfAbsent(uuid, k -> new PlayerState(null));
    }

    public void removeEntry(UUID uuid) {
        stateMap.remove(uuid);
    }

}
