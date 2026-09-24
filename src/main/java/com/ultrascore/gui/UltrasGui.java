package com.ultrascore.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Contract every UltrasCore inventory GUI implements. GuiManager tracks which
 * GUI instance each online player currently has open and routes clicks to it.
 */
public interface UltrasGui {

    Inventory getInventory();

    /** Called for any click while this GUI is open. Implementations must cancel the event themselves if needed. */
    void onClick(InventoryClickEvent event, Player player);

    /** Called when the player closes the inventory (optional cleanup hook). */
    default void onClose(Player player) {}
}
