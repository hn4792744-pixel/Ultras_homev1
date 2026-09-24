package com.ultrascore.nightvision;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NightVisionListener implements Listener {

    private final NightVisionManager manager;

    public NightVisionListener(NightVisionManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        manager.apply(event.getPlayer());
    }
}
