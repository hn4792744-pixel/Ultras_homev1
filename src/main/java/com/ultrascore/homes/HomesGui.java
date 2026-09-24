package com.ultrascore.homes;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.gui.GuiButton;
import com.ultrascore.gui.PaginatedGui;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Shows the homes belonging to {@code ownerUuid}. When the viewer IS the owner,
 * each bed has a matching "dye" (delete) button in the row below it, per spec.
 * When an admin opens someone else's homes (/home_admin), it becomes view/teleport
 * only — no delete controls, since deleting another player's home isn't in scope here.
 */
public class HomesGui extends PaginatedGui {

    private final UltrasCore plugin;
    private final HomeManager manager;
    private final Player viewer;
    private final UUID ownerUuid;
    private final boolean ownerView;

    public HomesGui(UltrasCore plugin, HomeManager manager, Player viewer, UUID ownerUuid) {
        super(6, titleFor(plugin, viewer, ownerUuid));
        this.plugin = plugin;
        this.manager = manager;
        this.viewer = viewer;
        this.ownerUuid = ownerUuid;
        this.ownerView = viewer.getUniqueId().equals(ownerUuid);
        render();
    }

    private static net.kyori.adventure.text.Component titleFor(UltrasCore plugin, Player viewer, UUID ownerUuid) {
        if (viewer.getUniqueId().equals(ownerUuid)) {
            return MessageUtil.render(plugin.getLanguageManager().get("homes.gui-title"));
        }
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUuid);
        return MessageUtil.render(plugin.getLanguageManager()
                .get("homes.admin-gui-title", "player", owner.getName() != null ? owner.getName() : ownerUuid.toString()));
    }

    @Override
    protected List<GuiButton> getAllButtons() {
        List<GuiButton> buttons = new ArrayList<>();
        Map<String, Home> homes = manager.getHomes(ownerUuid);
        for (Home home : homes.values()) {
            ItemStack bed = new ItemStack(Material.RED_BED);
            ItemMeta meta = bed.getItemMeta();
            if (meta != null) {
                meta.displayName(MessageUtil.render("<white>" + home.getName()));
                if (ownerView) {
                    meta.lore(List.of(
                            MessageUtil.render("<gray>Click to teleport"),
                            MessageUtil.render("<gray>Shift-click to delete")
                    ));
                } else {
                    meta.lore(List.of(MessageUtil.render("<gray>Click to teleport there")));
                }
                bed.setItemMeta(meta);
            }
            buttons.add(new GuiButton(bed, p -> {
                p.closeInventory();
                if (ownerView) {
                    manager.teleportToHome(p, home.getName());
                } else {
                    // Admin teleport-to-player's-home: bypasses delay/cooldown by design.
                    org.bukkit.Location loc = home.toLocation();
                    if (loc != null) p.teleport(loc);
                }
            }));
        }
        return buttons;
    }

    @Override
    public void onClick(org.bukkit.event.inventory.InventoryClickEvent event, Player player) {
        // Shift-click on a home = quick delete, only in owner view.
        if (ownerView && event.isShiftClick() && event.getRawSlot() < contentSlots) {
            event.setCancelled(true);
            List<GuiButton> all = getAllButtons();
            int index = page * contentSlots + event.getRawSlot();
            Map<String, Home> homes = manager.getHomes(ownerUuid);
            List<String> names = new ArrayList<>(homes.keySet());
            if (index >= 0 && index < names.size()) {
                String name = names.get(index);
                manager.deleteHome(ownerUuid, name);
                MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("homes.deleted", "name", name));
                render();
            }
            return;
        }
        super.onClick(event, player);
    }
}
