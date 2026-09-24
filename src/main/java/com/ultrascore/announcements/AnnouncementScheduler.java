package com.ultrascore.announcements;

import com.ultrascore.core.UltrasCore;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/** Cycles through systems.announcements.messages on a timer, if systems.announcements.enabled. */
public class AnnouncementScheduler {

    private final UltrasCore plugin;
    private final AnnouncementManager manager;
    private BukkitTask task;
    private int index = 0;

    public AnnouncementScheduler(UltrasCore plugin, AnnouncementManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        start();
    }

    private void start() {
        if (!plugin.getConfigManager().getBool("systems.announcements.enabled", false)) return;

        int intervalSeconds = plugin.getConfigManager().getInt("systems.announcements.interval-seconds", 900);
        long ticks = Math.max(20L * 30, intervalSeconds * 20L); // never faster than every 30s, avoids config-typo spam

        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            List<String> messages = plugin.getConfigManager().raw().getStringList("systems.announcements.messages");
            if (messages.isEmpty()) return;
            List<String> locations = plugin.getConfigManager().raw().getStringList("systems.announcements.locations");
            if (locations.isEmpty()) locations = List.of("CHAT");

            String message = messages.get(index % messages.size());
            index++;
            manager.send(Bukkit.getOnlinePlayers(), message, locations, 5, null);
        }, ticks, ticks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
