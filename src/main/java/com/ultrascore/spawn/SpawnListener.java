package com.ultrascore.spawn;

import com.ultrascore.core.UltrasCore;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

/** Sends the player to spawn on respawn, if systems.spawn.death-teleport is enabled and spawn is set. */
public class SpawnListener implements Listener {

    private final UltrasCore plugin;
    private final SpawnManager manager;

    public SpawnListener(UltrasCore plugin, SpawnManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!plugin.getConfigManager().getBool("systems.spawn.death-teleport", true)) return;
        Location spawn = manager.getSpawn();
        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }
    }
}
