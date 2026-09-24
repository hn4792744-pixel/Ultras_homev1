package com.ultrascore.gui;

import com.ultrascore.core.UltrasCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry of "who has which UltrasCore GUI open right now". A single
 * listener handles every GUI in the plugin instead of each screen registering
 * its own listener, keeping the event footprint small and consistent.
 */
public class GuiManager implements Listener {

    private final Map<UUID, UltrasGui> open = new ConcurrentHashMap<>();

    public GuiManager(UltrasCore plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player, UltrasGui gui) {
        open.put(player.getUniqueId(), gui);
        player.openInventory(gui.getInventory());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UltrasGui gui = open.get(player.getUniqueId());
        if (gui == null) return;
        if (!event.getInventory().equals(gui.getInventory())) return;
        gui.onClick(event, player);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        UltrasGui gui = open.remove(player.getUniqueId());
        if (gui != null) {
            gui.onClose(player);
        }
    }
}
