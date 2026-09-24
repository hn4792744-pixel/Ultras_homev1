package com.ultrascore.warps;

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

public class WarpGui extends PaginatedGui {

    private final UltrasCore plugin;
    private final WarpManager manager;
    private final Player viewer;

    public WarpGui(UltrasCore plugin, WarpManager manager, Player viewer) {
        super(6, MessageUtil.render(plugin.getLanguageManager().get("warps.gui-title")));
        this.plugin = plugin;
        this.manager = manager;
        this.viewer = viewer;
        render();
    }

    @Override
    protected List<GuiButton> getAllButtons() {
        List<GuiButton> buttons = new ArrayList<>();
        boolean bypass = viewer.hasPermission("ultrascore.warp.bypass");
        for (Warp warp : manager.all().values()) {
            boolean visible = warp.canAccess(viewer.getUniqueId(), bypass) || warp.getType() == Warp.Type.PASSWORD;
            if (!visible) continue;

            Material material = switch (warp.getType()) {
                case PUBLIC -> Material.GRASS_BLOCK;
                case PRIVATE -> Material.IRON_DOOR;
                case PASSWORD -> Material.TRIPWIRE_HOOK;
            };
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(MessageUtil.render("<white>" + warp.getName()));
                meta.lore(List.of(
                        MessageUtil.render("<gray>World: <white>" + warp.getWorld()),
                        MessageUtil.render("<gray>Type: <white>" + warp.getType())
                ));
                item.setItemMeta(meta);
            }
            buttons.add(new GuiButton(item, p -> {
                p.closeInventory();
                manager.attemptTeleport(p, warp);
            }));
        }
        return buttons;
    }
}
