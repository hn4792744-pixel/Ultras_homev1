package com.ultrascore.settings;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.gui.UltrasGui;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomGui implements UltrasGui {

    private final UltrasCore plugin;
    private final CustomGuiManager manager;
    private final CustomGuiDefinition def;
    private final Inventory inventory;
    private final Map<Integer, CustomGuiButton> slotMap = new HashMap<>();

    public CustomGui(UltrasCore plugin, CustomGuiManager manager, CustomGuiDefinition def, Player viewer) {
        this.plugin = plugin;
        this.manager = manager;
        this.def = def;
        this.inventory = Bukkit.createInventory(null, def.size, MessageUtil.render(def.title));

        for (CustomGuiButton button : def.buttons) {
            if (button.permission != null && !viewer.hasPermission(button.permission)) continue;
            if (button.slot < 0 || button.slot >= def.size) continue;

            Material material;
            try {
                material = Material.valueOf(button.material.toUpperCase());
            } catch (IllegalArgumentException e) {
                material = Material.STONE;
            }
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(MessageUtil.render(button.name));
                if (!button.lore.isEmpty()) {
                    meta.lore(button.lore.stream().map(MessageUtil::render).toList());
                }
                item.setItemMeta(meta);
            }
            inventory.setItem(button.slot, item);
            slotMap.put(button.slot, button);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        CustomGuiButton button = slotMap.get(event.getRawSlot());
        if (button == null) return;

        if (button.sound != null) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(button.sound), 1f, 1f);
            } catch (IllegalArgumentException ignored) {}
        }

        if ("open_settings".equalsIgnoreCase(button.action)) {
            player.closeInventory();
            plugin.getGuiManager().open(player, new SettingsGui(plugin, plugin.getSettingsManager(), player));
            return;
        }
        if (button.targetGui != null && manager.exists(button.targetGui)) {
            player.closeInventory();
            manager.open(player, button.targetGui);
            return;
        }
        if (button.command != null && !button.command.isBlank()) {
            player.closeInventory();
            Bukkit.getScheduler().runTask(plugin, () -> player.performCommand(button.command));
        }
    }
}
