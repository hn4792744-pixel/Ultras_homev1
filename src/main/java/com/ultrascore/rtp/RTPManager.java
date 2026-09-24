package com.ultrascore.rtp;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Finds a random safe location without ever running a big block-scan loop on
 * the main thread synchronously: each attempt asks Paper to load the target
 * chunk asynchronously (World#getChunkAtAsync), and only the cheap safety
 * check + eventual teleport happens on the main thread, in the chunk-load
 * callback. If an attempt is unsafe, the next attempt is scheduled the same
 * way — so a full search of N attempts is spread across chunk loads instead
 * of blocking a single tick.
 */
public class RTPManager {

    private final UltrasCore plugin;
    private final Map<UUID, Long> cooldownUntil = new ConcurrentHashMap<>();

    public RTPManager(UltrasCore plugin) {
        this.plugin = plugin;
    }

    private int minRadius() { return plugin.getConfigManager().getInt("systems.rtp.min-radius", 200); }
    private int maxRadius() { return plugin.getConfigManager().getInt("systems.rtp.max-radius", 5000); }
    private int attempts() { return plugin.getConfigManager().getInt("systems.rtp.attempts", 30); }
    private int delaySeconds() { return plugin.getConfigManager().getInt("systems.rtp.teleport-delay-seconds", 3); }
    private int cooldownSeconds() { return plugin.getConfigManager().getInt("systems.rtp.cooldown-seconds", 60); }
    private boolean cancelOnMove() { return plugin.getConfigManager().getBool("systems.rtp.cancel-on-move", true); }
    private boolean soundsEnabled() { return plugin.getConfigManager().getBool("systems.rtp.sounds.enabled", true); }

    public boolean isWorldAllowed(String worldName) {
        List<String> worlds = plugin.getConfigManager().raw().getStringList("systems.rtp.worlds");
        return worlds.isEmpty() || worlds.contains(worldName);
    }

    public boolean isCommandWorldAllowed(String worldName) {
        List<String> worlds = plugin.getConfigManager().raw().getStringList("systems.rtp.allowed-command-worlds");
        return worlds.isEmpty() || worlds.contains(worldName);
    }

    public boolean isOnCooldown(Player player) {
        if (player.hasPermission("ultrascore.rtp.bypass")) return false;
        Long until = cooldownUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    public long cooldownRemainingSeconds(Player player) {
        Long until = cooldownUntil.get(player.getUniqueId());
        if (until == null) return 0;
        return Math.max(0, (until - System.currentTimeMillis()) / 1000L);
    }

    public void teleportRandomly(Player player, World world) {
        if (!isWorldAllowed(world.getName())) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("rtp.world-not-allowed"));
            return;
        }
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("rtp.searching"));
        searchAttempt(player, world, attempts());
    }

    private void searchAttempt(Player player, World world, int attemptsLeft) {
        if (!player.isOnline()) return;
        if (attemptsLeft <= 0) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("rtp.no-safe-location"));
            return;
        }

        int min = minRadius();
        int max = Math.max(min + 1, maxRadius());
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double angle = rnd.nextDouble() * Math.PI * 2;
        int radius = rnd.nextInt(min, max);
        int x = (int) (Math.cos(angle) * radius);
        int z = (int) (Math.sin(angle) * radius);

        world.getChunkAtAsync(x >> 4, z >> 4).thenAccept(chunk -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            int y = world.getHighestBlockYAt(x, z);
            Location candidate = new Location(world, x + 0.5, y + 1, z + 0.5);

            if (SafeLocationFinder.isSafe(candidate)) {
                startCountdown(player, candidate);
            } else {
                searchAttempt(player, world, attemptsLeft - 1);
            }
        }));
    }

    private void startCountdown(Player player, Location destination) {
        if (player.hasPermission("ultrascore.rtp.bypass")) {
            player.teleport(destination);
            applyCooldown(player);
            playSound(player);
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("rtp.success"));
            return;
        }

        plugin.getTeleportCountdown().start(
                player,
                () -> destination,
                delaySeconds(),
                cancelOnMove(),
                (p, secondsLeft) -> MessageUtil.sendPrefixed(plugin, p, plugin.getLanguageManager()
                        .get("rtp.teleporting-in", "seconds", String.valueOf(secondsLeft))),
                p -> MessageUtil.sendPrefixed(plugin, p, plugin.getLanguageManager().get("tpa.teleport-cancelled-move")),
                p -> {
                    applyCooldown(p);
                    playSound(p);
                    MessageUtil.sendPrefixed(plugin, p, plugin.getLanguageManager().get("rtp.success"));
                }
        );
    }

    private void applyCooldown(Player player) {
        if (player.hasPermission("ultrascore.rtp.bypass")) return;
        cooldownUntil.put(player.getUniqueId(), System.currentTimeMillis() + cooldownSeconds() * 1000L);
    }

    private void playSound(Player player) {
        if (!soundsEnabled()) return;
        String name = plugin.getConfigManager().getString("systems.rtp.sounds.rtp-success", null);
        if (name == null) return;
        try {
            Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(name.toLowerCase()));
            if (sound != null) player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (IllegalArgumentException ignored) {
            // Invalid sound name in config — skip rather than throw.
        }
    }
}
