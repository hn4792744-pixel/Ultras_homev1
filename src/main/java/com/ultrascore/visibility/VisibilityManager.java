package com.ultrascore.visibility;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.settings.SettingKey;
import com.ultrascore.settings.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class VisibilityManager {

    private final UltrasCore plugin;
    private final SettingsManager settings;

    public VisibilityManager(UltrasCore plugin, SettingsManager settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public boolean isHiding(Player viewer) {
        return settings.get(viewer.getUniqueId(), SettingKey.HIDE_PLAYERS);
    }

    public void setHiding(Player viewer, boolean hiding) {
        settings.set(viewer.getUniqueId(), SettingKey.HIDE_PLAYERS, hiding);
        applyFor(viewer);
    }

    public boolean toggle(Player viewer) {
        boolean next = !isHiding(viewer);
        setHiding(viewer, next);
        return next;
    }

    /** Re-applies {@code viewer}'s current hide/show state against every other online player. */
    public void applyFor(Player viewer) {
        boolean hiding = isHiding(viewer);
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(viewer)) continue;
            applyPair(viewer, other, hiding);
        }
    }

    /** Called when someone else joins, so we re-apply every online viewer's preference onto the newcomer. */
    public void applyAllTo(Player newcomer) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(newcomer)) continue;
            if (isHiding(viewer)) {
                applyPair(viewer, newcomer, true);
            }
        }
    }

    private void applyPair(Player viewer, Player target, boolean hiding) {
        if (!hiding) {
            viewer.showPlayer(plugin, target);
            return;
        }
        if (!eligible(viewer, target)) {
            viewer.showPlayer(plugin, target);
            return;
        }
        viewer.hidePlayer(plugin, target);
    }

    private boolean eligible(Player viewer, Player target) {
        boolean hideOp = plugin.getConfigManager().getBool("systems.visibility.hide-op", false);
        if (target.isOp() && !hideOp) return false;

        boolean sameWorldOnly = plugin.getConfigManager().getBool("systems.visibility.same-world-only", false);
        if (sameWorldOnly && !viewer.getWorld().equals(target.getWorld())) return false;

        List<String> whitelist = plugin.getConfigManager().raw().getStringList("systems.visibility.worlds-whitelist");
        List<String> blacklist = plugin.getConfigManager().raw().getStringList("systems.visibility.worlds-blacklist");
        String world = target.getWorld().getName();
        if (!whitelist.isEmpty() && !whitelist.contains(world)) return false;
        if (blacklist.contains(world)) return false;

        return true;
    }
}
