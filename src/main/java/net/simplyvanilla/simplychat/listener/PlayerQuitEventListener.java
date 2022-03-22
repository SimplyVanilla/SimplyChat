package net.simplyvanilla.simplychat.listener;

import net.simplyvanilla.simplychat.state.PlayerStateManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventListener implements Listener {

    private final PlayerStateManager playerStateManager;

    public PlayerQuitEventListener(PlayerStateManager playerStateManager) {
        this.playerStateManager = playerStateManager;
    }

    @EventHandler
    public void handlePlayerQuitEvent(PlayerQuitEvent event) {
        playerStateManager.removeEntry(event.getPlayer().getUniqueId());
    }

}
