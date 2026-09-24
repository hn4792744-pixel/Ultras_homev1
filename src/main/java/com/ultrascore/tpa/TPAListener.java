package com.ultrascore.tpa;

import com.ultrascore.core.UltrasCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Movement-based cancellation is handled centrally by TeleportCountdown; this
 * listener only clears pending requests / cooldown lookups tied to a player
 * who has left, so stale entries can't leak or resolve against an offline player.
 */
public class TPAListener implements Listener {

    private final UltrasCore plugin;
    private final TPAManager manager;

    public TPAListener(UltrasCore plugin, TPAManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.clearFor(event.getPlayer().getUniqueId());
    }
}
