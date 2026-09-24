package com.ultrascore.tpa;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns all TPA/TPAHere state: pending requests (one active incoming request per
 * target, latest wins — matches the spec's simple accept/deny/cancel flow),
 * per-player "don't send me requests" toggle, and cooldowns. Actual walking-
 * distance teleport countdown is delegated to the shared TeleportCountdown util.
 */
public class TPAManager {

    private final UltrasCore plugin;
    private final Map<UUID, TPARequest> pendingByTarget = new ConcurrentHashMap<>();
    private final Set<UUID> notReceiving = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> cooldownUntil = new ConcurrentHashMap<>();

    public TPAManager(UltrasCore plugin) {
        this.plugin = plugin;
    }

    private int delaySeconds() {
        return plugin.getConfigManager().getInt("systems.tpa.teleport-delay-seconds", 3);
    }

    private int cooldownSeconds() {
        return plugin.getConfigManager().getInt("systems.tpa.cooldown-seconds", 60);
    }

    private int expireSeconds() {
        return plugin.getConfigManager().getInt("systems.tpa.request-expire-seconds", 60);
    }

    private boolean cancelOnMove() {
        return plugin.getConfigManager().getBool("systems.tpa.cancel-on-move", true);
    }

    private boolean soundsEnabled() {
        return plugin.getConfigManager().getBool("systems.tpa.sounds.enabled", true);
    }

    public boolean isReceiving(UUID uuid) {
        return !notReceiving.contains(uuid);
    }

    public void toggleReceiving(Player player) {
        UUID id = player.getUniqueId();
        if (notReceiving.contains(id)) {
            notReceiving.remove(id);
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("tpa.toggled-on"));
        } else {
            notReceiving.add(id);
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("tpa.toggled-off"));
        }
    }

    public boolean isOnCooldown(Player player) {
        if (player.hasPermission("ultrascore.tpa.bypass")) return false;
        Long until = cooldownUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    public long cooldownRemainingSeconds(Player player) {
        Long until = cooldownUntil.get(player.getUniqueId());
        if (until == null) return 0;
        return Math.max(0, (until - System.currentTimeMillis()) / 1000L);
    }

    public void sendRequest(Player requester, Player target, TPARequest.Type type) {
        if (!isReceiving(target.getUniqueId())) {
            MessageUtil.sendPrefixed(plugin, requester, plugin.getLanguageManager()
                    .get("general.player-not-found"));
            return;
        }
        TPARequest request = new TPARequest(requester.getUniqueId(), target.getUniqueId(), type);
        pendingByTarget.put(target.getUniqueId(), request);

        MessageUtil.sendPrefixed(plugin, requester, plugin.getLanguageManager()
                .get("tpa.sent", "player", target.getName()));

        String key = type == TPARequest.Type.HERE ? "tpa.received-here" : "tpa.received";
        MessageUtil.sendPrefixed(plugin, target, plugin.getLanguageManager().get(key, "player", requester.getName()));
        playSound(target, "request");

        // Auto-expire.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            TPARequest current = pendingByTarget.get(target.getUniqueId());
            if (current == request) {
                pendingByTarget.remove(target.getUniqueId());
                Player r = Bukkit.getPlayer(request.getRequester());
                if (r != null && r.isOnline()) {
                    MessageUtil.sendPrefixed(plugin, r, plugin.getLanguageManager()
                            .get("tpa.expired", "player", target.getName()));
                }
            }
        }, expireSeconds() * 20L);
    }

    public void accept(Player target) {
        TPARequest request = pendingByTarget.remove(target.getUniqueId());
        if (request == null) {
            MessageUtil.sendPrefixed(plugin, target, plugin.getLanguageManager().get("tpa.none-pending"));
            return;
        }
        if (request.isExpired(expireSeconds())) {
            MessageUtil.sendPrefixed(plugin, target, plugin.getLanguageManager().get("tpa.none-pending"));
            return;
        }
        Player requester = Bukkit.getPlayer(request.getRequester());
        if (requester == null || !requester.isOnline()) {
            MessageUtil.sendPrefixed(plugin, target, plugin.getLanguageManager().get("general.player-not-found"));
            return;
        }

        MessageUtil.sendPrefixed(plugin, requester, plugin.getLanguageManager().get("tpa.accepted", "player", target.getName()));
        playSound(requester, "accept");

        Player moving = request.getType() == TPARequest.Type.TO ? requester : target;
        Player anchor = request.getType() == TPARequest.Type.TO ? target : requester;

        startCountdown(moving, anchor);
    }

    public void deny(Player target) {
        TPARequest request = pendingByTarget.remove(target.getUniqueId());
        if (request == null) {
            MessageUtil.sendPrefixed(plugin, target, plugin.getLanguageManager().get("tpa.none-pending"));
            return;
        }
        Player requester = Bukkit.getPlayer(request.getRequester());
        if (requester != null && requester.isOnline()) {
            MessageUtil.sendPrefixed(plugin, requester, plugin.getLanguageManager().get("tpa.denied", "player", target.getName()));
        }
    }

    public void cancel(Player requester) {
        boolean removed = pendingByTarget.values().removeIf(r -> r.getRequester().equals(requester.getUniqueId()));
        if (removed) {
            MessageUtil.sendPrefixed(plugin, requester, plugin.getLanguageManager().get("tpa.cancelled"));
        } else {
            MessageUtil.sendPrefixed(plugin, requester, plugin.getLanguageManager().get("tpa.none-pending"));
        }
    }

    private void startCountdown(Player moving, Player anchor) {
        if (moving.hasPermission("ultrascore.tpa.bypass")) {
            moving.teleport(anchor.getLocation());
            applyCooldown(moving);
            playSound(moving, "teleport-success");
            return;
        }

        plugin.getTeleportCountdown().start(
                moving,
                anchor::getLocation,
                delaySeconds(),
                cancelOnMove(),
                (p, secondsLeft) -> MessageUtil.sendPrefixed(plugin, p, plugin.getLanguageManager()
                        .get("tpa.teleporting-in", "seconds", String.valueOf(secondsLeft))),
                p -> MessageUtil.sendPrefixed(plugin, p, plugin.getLanguageManager().get("tpa.teleport-cancelled-move")),
                p -> {
                    applyCooldown(p);
                    playSound(p, "teleport-success");
                }
        );
    }

    private void applyCooldown(Player player) {
        if (player.hasPermission("ultrascore.tpa.bypass")) return;
        cooldownUntil.put(player.getUniqueId(), System.currentTimeMillis() + cooldownSeconds() * 1000L);
    }

    private void playSound(Player player, String key) {
        if (!soundsEnabled()) return;
        String name = plugin.getConfigManager().getString("systems.tpa.sounds." + key, null);
        if (name == null) return;
        try {
            Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(name.toLowerCase()));
            if (sound != null) player.playSound(player.getLocation(), sound, 1f, 1f);
        } catch (IllegalArgumentException ignored) {
            // Invalid sound name in config — silently skip rather than throw.
        }
    }

    public void clearFor(UUID uuid) {
        pendingByTarget.remove(uuid);
        pendingByTarget.values().removeIf(r -> r.getRequester().equals(uuid));
        plugin.getTeleportCountdown().cancel(uuid);
    }
}
