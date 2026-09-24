package com.ultrascore.visibility;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VisibilityListener implements Listener {

    private final VisibilityManager manager;

    public VisibilityListener(VisibilityManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        // The newcomer's own hide-players preference against everyone already online...
        manager.applyFor(event.getPlayer());
        // ...and everyone already online who has hide-players on, against the newcomer.
        manager.applyAllTo(event.getPlayer());
    }
}
