package com.ultrascore.homes;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.storage.StorageManager;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class HomeManager {

    private static final String CATEGORY = "homes";

    private final UltrasCore plugin;
    private final StorageManager storage;

    public HomeManager(UltrasCore plugin, StorageManager storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public Map<String, Home> getHomes(UUID uuid) {
        YamlConfiguration doc = storage.get(CATEGORY, uuid);
        ConfigurationSection section = doc.getConfigurationSection("homes");
        Map<String, Home> homes = new LinkedHashMap<>();
        if (section == null) return homes;
        for (String name : section.getKeys(false)) {
            ConfigurationSection h = section.getConfigurationSection(name);
            if (h == null) continue;
            homes.put(name, new Home(
                    name,
                    h.getString("world", "world"),
                    h.getDouble("x"), h.getDouble("y"), h.getDouble("z"),
                    (float) h.getDouble("yaw"), (float) h.getDouble("pitch")
            ));
        }
        return homes;
    }

    public Home getHome(UUID uuid, String name) {
        return getHomes(uuid).get(name);
    }

    public boolean hasHome(UUID uuid, String name) {
        return getHome(uuid, name) != null;
    }

    /** @return true if set, false if the player is already at their home limit. */
    public boolean setHome(Player player, String name, org.bukkit.Location location) {
        UUID uuid = player.getUniqueId();
        Map<String, Home> homes = getHomes(uuid);
        boolean isNew = !homes.containsKey(name);
        if (isNew && homes.size() >= getLimit(player)) {
            return false;
        }
        YamlConfiguration doc = storage.get(CATEGORY, uuid);
        String base = "homes." + name;
        doc.set(base + ".world", location.getWorld().getName());
        doc.set(base + ".x", location.getX());
        doc.set(base + ".y", location.getY());
        doc.set(base + ".z", location.getZ());
        doc.set(base + ".yaw", (double) location.getYaw());
        doc.set(base + ".pitch", (double) location.getPitch());
        storage.markDirtyAndSaveAsync(CATEGORY, uuid);
        return true;
    }

    public boolean deleteHome(UUID uuid, String name) {
        if (!hasHome(uuid, name)) return false;
        YamlConfiguration doc = storage.get(CATEGORY, uuid);
        doc.set("homes." + name, null);
        storage.markDirtyAndSaveAsync(CATEGORY, uuid);
        return true;
    }

    public int getLimit(Player player) {
        YamlConfiguration doc = storage.get(CATEGORY, player.getUniqueId());
        if (doc.contains("limit-override")) {
            return doc.getInt("limit-override");
        }
        if (player.hasPermission("ultrascore.homes.bypass") || player.isOp()) {
            return plugin.getConfigManager().getInt("systems.homes.op-limit", 20);
        }
        return plugin.getConfigManager().getInt("systems.homes.default-limit", 3);
    }

    public void setLimitOverride(UUID uuid, int amount) {
        YamlConfiguration doc = storage.get(CATEGORY, uuid);
        doc.set("limit-override", Math.max(0, amount));
        storage.markDirtyAndSaveAsync(CATEGORY, uuid);
    }

    public void resetLimitOverride(UUID uuid) {
        YamlConfiguration doc = storage.get(CATEGORY, uuid);
        doc.set("limit-override", null);
        storage.markDirtyAndSaveAsync(CATEGORY, uuid);
    }

    public void teleportToHome(Player player, String name) {
        Home home = getHome(player.getUniqueId(), name);
        if (home == null) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("homes.not-found", "name", name));
            return;
        }
        int delay = plugin.getConfigManager().getInt("systems.homes.teleport-delay-seconds", 3);
        boolean cancelOnMove = plugin.getConfigManager().getBool("systems.homes.cancel-on-move", true);

        if (player.hasPermission("ultrascore.homes.bypass")) {
            org.bukkit.Location loc = home.toLocation();
            if (loc != null) player.teleport(loc);
            return;
        }

        plugin.getTeleportCountdown().start(
                player,
                home::toLocation,
                delay,
                cancelOnMove,
                (p, secondsLeft) -> MessageUtil.sendPrefixed(plugin, p, plugin.getLanguageManager()
                        .get("homes.teleporting", "seconds", String.valueOf(secondsLeft))),
                p -> MessageUtil.sendPrefixed(plugin, p, plugin.getLanguageManager().get("tpa.teleport-cancelled-move")),
                p -> {}
        );
    }
}
