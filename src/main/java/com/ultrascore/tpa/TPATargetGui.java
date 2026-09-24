package com.ultrascore.tpa;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.gui.GuiButton;
import com.ultrascore.gui.PaginatedGui;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/** First screen of /tpa and /tpahere with no arguments: pick a player from the list. */
public class TPATargetGui extends PaginatedGui {

    private final UltrasCore plugin;
    private final TPAManager manager;
    private final Player viewer;
    private final TPARequest.Type type;

    public TPATargetGui(UltrasCore plugin, TPAManager manager, Player viewer, TPARequest.Type type) {
        super(6, MessageUtil.render(plugin.getLanguageManager().get("tpa.gui-title")));
        this.plugin = plugin;
        this.manager = manager;
        this.viewer = viewer;
        this.type = type;
        render();
    }

    @Override
    protected List<GuiButton> getAllButtons() {
        List<GuiButton> buttons = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(viewer)) continue;
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            if (meta instanceof SkullMeta skullMeta) {
                skullMeta.setOwningPlayer(target);
                skullMeta.displayName(MessageUtil.render("<white>" + target.getName()));
                skullMeta.lore(List.of(
                        MessageUtil.render("<gray>World: <white>" + target.getWorld().getName()),
                        MessageUtil.render("<gray>Click to request teleport")
                ));
                head.setItemMeta(skullMeta);
            }
            buttons.add(new GuiButton(head, p -> {
                p.closeInventory();
                if (manager.isOnCooldown(p)) {
                    MessageUtil.sendPrefixed(plugin, p, plugin.getLanguageManager()
                            .get("tpa.cooldown", "seconds", String.valueOf(manager.cooldownRemainingSeconds(p))));
                    return;
                }
                manager.sendRequest(p, target, type);
            }));
        }
        return buttons;
    }
}
