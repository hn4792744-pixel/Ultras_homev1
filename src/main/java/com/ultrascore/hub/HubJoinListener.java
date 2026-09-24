package com.ultrascore.hub;

import com.ultrascore.core.UltrasCore;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/** If systems.hub.join-teleport is on and we're in LOCAL mode, send joining players straight to the hub. */
public class HubJoinListener implements Listener {

    private final UltrasCore plugin;
    private final HubManager manager;

    public HubJoinListener(UltrasCore plugin, HubManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (manager.isProxyMode()) return; // that's the proxy's job to route on first connect, not ours
        if (!plugin.getConfigManager().getBool("systems.hub.join-teleport", false)) return;
        Location hub = manager.getHub();
        if (hub != null) {
            event.getPlayer().teleport(hub);
        }
    }
}
