package com.ultrascore.settings;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.gui.GuiButton;
import com.ultrascore.gui.PaginatedGui;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** PaginatedGui already slices however many buttons it's given across as many pages as needed. */
public class SettingsGui extends PaginatedGui {

    private final UltrasCore plugin;
    private final SettingsManager manager;
    private final Player viewer;

    public SettingsGui(UltrasCore plugin, SettingsManager manager, Player viewer) {
        super(6, MessageUtil.render(plugin.getLanguageManager().get("settings.gui-title")));
        this.plugin = plugin;
        this.manager = manager;
        this.viewer = viewer;
        render();
    }

    @Override
    protected List<GuiButton> getAllButtons() {
        List<GuiButton> buttons = new ArrayList<>();
        for (SettingKey key : SettingKey.all().values()) {
            boolean value = manager.get(viewer.getUniqueId(), key);
            ItemStack item = new ItemStack(value ? Material.LIME_DYE : Material.GRAY_DYE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(MessageUtil.render("<white>" + key.getLabel()));
                meta.lore(List.of(MessageUtil.render(value ? "<green>ON — click to disable" : "<red>OFF — click to enable")));
                item.setItemMeta(meta);
            }
            buttons.add(new GuiButton(item, p -> {
                boolean next = manager.toggle(p.getUniqueId(), key);
                String msgKey = next ? "settings.toggled-on" : "settings.toggled-off";
                MessageUtil.sendPrefixed(plugin, p, plugin.getLanguageManager().get(msgKey, "setting", key.getLabel()));
                render();
                p.updateInventory();
            }));
        }
        return buttons;
    }
}
