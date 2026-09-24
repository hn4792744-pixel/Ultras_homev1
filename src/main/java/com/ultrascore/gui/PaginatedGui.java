package com.ultrascore.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Reusable paginated inventory GUI. Subclasses supply the full list of content
 * buttons (one per "entry" — a player, a home, a warp…) and this class handles
 * slicing per page and rendering Next / Previous / Close controls in the bottom row.
 *
 * Layout: rows 0..(rows-2) are content (contentSlots = (rows-1) * 9), the last
 * row holds navigation at slots [size-9+2]=Previous, [size-9+4]=Close, [size-9+6]=Next.
 */
public abstract class PaginatedGui implements UltrasGui {

    protected final int rows;
    protected final int size;
    protected final int contentSlots;
    protected final Component title;
    protected int page = 0;
    protected Inventory inventory;

    protected PaginatedGui(int rows, Component title) {
        this.rows = Math.max(2, Math.min(6, rows));
        this.size = this.rows * 9;
        this.contentSlots = (this.rows - 1) * 9;
        this.title = title;
        this.inventory = Bukkit.createInventory(null, size, title);
        // NOTE: render() is deliberately NOT called here. getAllButtons() is
        // overridden by subclasses and typically reads subclass fields, which are
        // not yet assigned while the superclass constructor is still running.
        // Subclasses must call render() themselves at the end of their own
        // constructor, once all their fields are set.
    }

    /** Full list of buttons across all pages; called each render, may be dynamic. */
    protected abstract List<GuiButton> getAllButtons();

    /** Optional: called when player clicks Close (default: just closes inventory). */
    protected void onCloseButton(Player player) {
        player.closeInventory();
    }

    protected void render() {
        inventory.clear();
        List<GuiButton> all = getAllButtons();
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) contentSlots));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        int from = page * contentSlots;
        int to = Math.min(all.size(), from + contentSlots);
        for (int i = from; i < to; i++) {
            inventory.setItem(i - from, all.get(i).getIcon());
        }

        int navRow = size - 9;
        if (page > 0) {
            inventory.setItem(navRow + 2, namedItem(Material.ARROW, "<yellow>« Previous Page"));
        }
        inventory.setItem(navRow + 4, namedItem(Material.BARRIER, "<red>Close"));
        if (page < totalPages - 1) {
            inventory.setItem(navRow + 6, namedItem(Material.ARROW, "<yellow>Next Page »"));
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= size) return;

        int navRow = size - 9;
        if (slot == navRow + 2) {
            page--;
            render();
            return;
        }
        if (slot == navRow + 4) {
            onCloseButton(player);
            return;
        }
        if (slot == navRow + 6) {
            page++;
            render();
            return;
        }
        if (slot >= contentSlots) return;

        List<GuiButton> all = getAllButtons();
        int index = page * contentSlots + slot;
        if (index >= 0 && index < all.size()) {
            all.get(index).click(player);
        }
    }

    private ItemStack namedItem(Material material, String miniMessageName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(com.ultrascore.utils.MessageUtil.render(miniMessageName));
            item.setItemMeta(meta);
        }
        return item;
    }
}
