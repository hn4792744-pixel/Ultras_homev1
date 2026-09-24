package com.ultrascore.announcements;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.settings.SettingKey;
import com.ultrascore.settings.SettingsManager;
import com.ultrascore.utils.MessageUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * A single delivery routine shared by /bc, /broadcast and the optional automatic
 * announcement scheduler: renders one MiniMessage string once and fans it out to
 * whichever locations (CHAT/TITLE/ACTIONBAR/BOSSBAR) were requested.
 */
public class AnnouncementManager {

    private final UltrasCore plugin;
    private final SettingsManager settings;

    public AnnouncementManager(UltrasCore plugin, SettingsManager settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void send(Collection<? extends Player> targets, String rawMessage, List<String> locations,
                      int durationSeconds, String soundName) {
        String prefixed = plugin.getConfigManager().getServerPrefix() + " " + rawMessage;
        Component chatComponent = MessageUtil.render(prefixed);
        Component plain = MessageUtil.render(rawMessage);

        for (Player player : targets) {
            if (!settings.get(player.getUniqueId(), SettingKey.ANNOUNCEMENTS)) continue;

            for (String location : locations) {
                switch (location.toUpperCase(Locale.ROOT)) {
                    case "CHAT" -> player.sendMessage(chatComponent);
                    case "ACTIONBAR" -> player.sendActionBar(plain);
                    case "TITLE" -> player.showTitle(Title.title(
                            plain, Component.empty(),
                            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(Math.max(1, durationSeconds)), Duration.ofMillis(500))));
                    case "BOSSBAR" -> showBossBar(player, plain, durationSeconds);
                    default -> plugin.getLogManager().warn("announcements", "Unknown broadcast location: " + location);
                }
            }

            if (soundName != null && !soundName.isBlank()) {
                try {
                    player.playSound(player.getLocation(), Sound.valueOf(soundName), 1f, 1f);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    private void showBossBar(Player player, Component text, int durationSeconds) {
        BossBar bar = BossBar.bossBar(text, 1f, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        player.showBossBar(bar);
        int ticks = Math.max(20, durationSeconds * 20);
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.hideBossBar(bar), ticks);
    }
}
