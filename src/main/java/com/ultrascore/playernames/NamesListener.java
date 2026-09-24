package com.ultrascore.playernames;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NamesListener implements Listener {

    private final NamesManager manager;

    public NamesListener(NamesManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        manager.applyFor(event.getPlayer());
        manager.registerNewcomerWithExistingViewers(event.getPlayer());
    }
}
