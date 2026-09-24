package com.ultrascore.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * One shared "teleport in N seconds, cancel if you move" engine, used by every
 * system that needs a delayed teleport (TPA, RTP, Homes, Warps...). Registered
 * once by the plugin so we only ever have a single PlayerMoveEvent listener for
 * this concern, instead of one per system.
 */
public class TeleportCountdown implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public TeleportCountdown(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Starts a countdown for {@code player}. {@code destination} is resolved fresh
     * when the countdown completes (so a TPA target that keeps moving still works).
     */
    public void start(Player player, Supplier<Location> destination, int delaySeconds, boolean cancelOnMove,
                       BiConsumer<Player, Integer> onTick, Consumer<Player> onCancel, Consumer<Player> onComplete) {
        cancel(player.getUniqueId());

        if (delaySeconds <= 0) {
            Location dest = destination.get();
            if (dest != null) player.teleport(dest);
            if (onComplete != null) onComplete.accept(player);
            return;
        }

        Session session = new Session(player.getLocation(), cancelOnMove);
        sessions.put(player.getUniqueId(), session);

        int[] remaining = {delaySeconds};
        session.task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                cancel(player.getUniqueId());
                return;
            }
            if (remaining[0] <= 0) {
                sessions.remove(player.getUniqueId());
                if (session.task != null) session.task.cancel();
                Location dest = destination.get();
                if (dest != null) player.teleport(dest);
                if (onComplete != null) onComplete.accept(player);
                return;
            }
            if (onTick != null) onTick.accept(player, remaining[0]);
            remaining[0]--;
        }, 0L, 20L);

        session.onCancel = onCancel;
    }

    public void cancel(UUID uuid) {
        Session session = sessions.remove(uuid);
        if (session != null && session.task != null) {
            session.task.cancel();
        }
    }

    public boolean hasActive(UUID uuid) {
        return sessions.containsKey(uuid);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Session session = sessions.get(player.getUniqueId());
        if (session == null || !session.cancelOnMove) return;

        Location from = session.origin;
        Location to = event.getTo();
        if (to == null) return;
        if (from.getWorld() != to.getWorld()
                || from.distanceSquared(to) > 0.04) { // ~0.2 blocks — ignores head-turn-only "movement"
            sessions.remove(player.getUniqueId());
            if (session.task != null) session.task.cancel();
            if (session.onCancel != null) session.onCancel.accept(player);
        }
    }

    private static final class Session {
        final Location origin;
        final boolean cancelOnMove;
        BukkitTask task;
        Consumer<Player> onCancel;

        Session(Location origin, boolean cancelOnMove) {
            this.origin = origin;
            this.cancelOnMove = cancelOnMove;
        }
    }
}
