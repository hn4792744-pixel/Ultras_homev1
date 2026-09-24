package com.ultrascore.warps;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns every Warp. Backed by a single warps.yml (warps are a small, admin-curated
 * data set — one shared file is simpler and cheaper than one-file-per-warp, and
 * is still saved asynchronously so a busy save never blocks gameplay).
 */
public class WarpManager {

    private final UltrasCore plugin;
    private final File file;
    private YamlConfiguration doc;
    private final Map<String, Warp> warps = new LinkedHashMap<>();
    private final Map<UUID, String> awaitingPassword = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldownUntil = new ConcurrentHashMap<>();

    public WarpManager(UltrasCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "warps.yml");
        load();
    }

    private void load() {
        warps.clear();
        if (file.exists()) {
            doc = YamlConfiguration.loadConfiguration(file);
        } else {
            doc = new YamlConfiguration();
        }
        ConfigurationSection root = doc.getConfigurationSection("warps");
        if (root == null) return;
        for (String name : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(name);
            if (s == null) continue;
            try {
                Warp warp = new Warp(name);
                warp.setLocation(new org.bukkit.Location(
                        Bukkit.getWorld(s.getString("world", "world")) != null
                                ? Bukkit.getWorld(s.getString("world", "world")) : Bukkit.getWorlds().get(0),
                        s.getDouble("x"), s.getDouble("y"), s.getDouble("z"),
                        (float) s.getDouble("yaw"), (float) s.getDouble("pitch")));
                warp.setType(Warp.Type.valueOf(s.getString("type", "PUBLIC")));
                warp.setPasswordHash(s.getString("password", null));
                String ownerStr = s.getString("owner", null);
                if (ownerStr != null) warp.setOwner(UUID.fromString(ownerStr));
                for (String m : s.getStringList("members")) {
                    try { warp.getMembers().add(UUID.fromString(m)); } catch (IllegalArgumentException ignored) {}
                }
                warps.put(name.toLowerCase(), warp);
            } catch (Exception e) {
                plugin.getLogManager().error("warps", "Skipping corrupt warp '" + name + "': " + e.getMessage());
            }
        }
    }

    public Warp get(String name) {
        return warps.get(name.toLowerCase());
    }

    public boolean exists(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public Map<String, Warp> all() {
        return warps;
    }

    public Warp create(String name, org.bukkit.Location location, UUID owner) {
        Warp warp = new Warp(name);
        warp.setLocation(location);
        warp.setOwner(owner);
        warps.put(name.toLowerCase(), warp);
        saveAsync();
        return warp;
    }

    public boolean remove(String name) {
        boolean removed = warps.remove(name.toLowerCase()) != null;
        if (removed) saveAsync();
        return removed;
    }

    public void save(Warp warp) {
        saveAsync();
    }

    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] out = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed available on every JVM; this branch is unreachable in practice.
            return Integer.toHexString(password.hashCode());
        }
    }

    // ---- Teleportation, access, passwords ----

    public boolean isOnCooldown(Player player) {
        if (player.hasPermission("ultrascore.warp.bypass")) return false;
        Long until = cooldownUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    public long cooldownRemainingSeconds(Player player) {
        Long until = cooldownUntil.get(player.getUniqueId());
        if (until == null) return 0;
        return Math.max(0, (until - System.currentTimeMillis()) / 1000L);
    }

    /** Entry point for both /warp <name> and clicking a warp in the GUI. */
    public void attemptTeleport(Player player, Warp warp) {
        boolean bypass = player.hasPermission("ultrascore.warp.bypass");

        if (!bypass && warp.getType() == Warp.Type.PASSWORD && !warp.canAccess(player.getUniqueId(), false)) {
            awaitingPassword.put(player.getUniqueId(), warp.getName());
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager()
                    .get("warps.enter-password", "warp", warp.getName()));
            return;
        }
        if (!warp.canAccess(player.getUniqueId(), bypass)) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.no-access"));
            return;
        }
        startTeleport(player, warp);
    }

    /** Called by the chat listener once a player who is awaiting a password types something. */
    public boolean isAwaitingPassword(UUID uuid) {
        return awaitingPassword.containsKey(uuid);
    }

    public void submitPassword(Player player, String rawInput) {
        String warpName = awaitingPassword.remove(player.getUniqueId());
        if (warpName == null) return;
        Warp warp = get(warpName);
        if (warp == null) return;

        String hash = hash(rawInput);
        if (hash.equals(warp.getPasswordHash())) {
            startTeleport(player, warp);
        } else {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.wrong-password"));
        }
    }

    private void startTeleport(Player player, Warp warp) {
        if (isOnCooldown(player)) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager()
                    .get("warps.cooldown", "seconds", String.valueOf(cooldownRemainingSeconds(player))));
            return;
        }
        boolean bypass = player.hasPermission("ultrascore.warp.bypass");
        int delay = plugin.getConfigManager().getInt("systems.warps.teleport-delay-seconds", 3);
        boolean cancelOnMove = plugin.getConfigManager().getBool("systems.warps.cancel-on-move", true);

        if (bypass) {
            org.bukkit.Location loc = warp.toLocation();
            if (loc != null) player.teleport(loc);
            playSound(player);
            return;
        }

        plugin.getTeleportCountdown().start(
                player,
                warp::toLocation,
                delay,
                cancelOnMove,
                (p, secondsLeft) -> MessageUtil.sendPrefixed(plugin, p, plugin.getLanguageManager()
                        .get("warps.teleporting", "warp", warp.getName(), "seconds", String.valueOf(secondsLeft))),
                p -> MessageUtil.sendPrefixed(plugin, p, plugin.getLanguageManager().get("tpa.teleport-cancelled-move")),
                p -> {
                    applyCooldown(p);
                    playSound(p);
                }
        );
    }

    private void applyCooldown(Player player) {
        if (player.hasPermission("ultrascore.warp.bypass")) return;
        int seconds = plugin.getConfigManager().getInt("systems.warps.cooldown-seconds", 60);
        cooldownUntil.put(player.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
    }

    private void playSound(Player player) {
        if (!plugin.getConfigManager().getBool("systems.warps.sounds.enabled", true)) return;
        String name = plugin.getConfigManager().getString("systems.warps.sounds.teleport-success", null);
        if (name == null) return;
        try {
            Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(name.toLowerCase()));
            if (sound != null) player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (IllegalArgumentException ignored) {
            // Invalid sound name in config — skip rather than throw.
        }
    }

    public void clearFor(UUID uuid) {
        awaitingPassword.remove(uuid);
    }

    public void saveAsync() {
        // Snapshot the current state onto the YAML document synchronously (cheap, in-memory),
        // then write the file asynchronously so disk I/O never blocks the main thread.
        doc.set("warps", null);
        for (Warp warp : warps.values()) {
            String base = "warps." + warp.getName();
            doc.set(base + ".world", warp.getWorld());
            doc.set(base + ".x", warp.getX());
            doc.set(base + ".y", warp.getY());
            doc.set(base + ".z", warp.getZ());
            doc.set(base + ".yaw", (double) warp.getYaw());
            doc.set(base + ".pitch", (double) warp.getPitch());
            doc.set(base + ".type", warp.getType().name());
            doc.set(base + ".password", warp.getPasswordHash());
            doc.set(base + ".owner", warp.getOwner() != null ? warp.getOwner().toString() : null);
            doc.set(base + ".members", warp.getMembers().stream().map(UUID::toString).toList());
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                doc.save(file);
            } catch (IOException e) {
                plugin.getLogManager().error("warps", "Failed to save warps.yml: " + e.getMessage());
            }
        });
    }
}
