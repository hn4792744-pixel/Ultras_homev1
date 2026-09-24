package com.ultrascore.warps;

import com.ultrascore.core.UltrasCore;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * When a player is prompted for a warp password (WarpManager#attemptTeleport),
 * their very next chat message is consumed here as the password instead of
 * being broadcast to chat.
 */
public class WarpPasswordListener implements Listener {

    private final UltrasCore plugin;
    private final WarpManager manager;

    public WarpPasswordListener(UltrasCore plugin, WarpManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        if (!manager.isAwaitingPassword(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> manager.submitPassword(event.getPlayer(), message));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.clearFor(event.getPlayer().getUniqueId());
    }
}
