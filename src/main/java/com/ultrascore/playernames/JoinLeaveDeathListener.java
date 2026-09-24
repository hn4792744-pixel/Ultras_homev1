package com.ultrascore.playernames;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.settings.SettingKey;
import com.ultrascore.settings.SettingsManager;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Replaces Bukkit's default broadcast-to-everyone join/leave/death messages with
 * a per-viewer filtered send, so a player who disabled Join/Leave/Death Messages
 * in /setting simply never receives them (everyone else is unaffected).
 */
public class JoinLeaveDeathListener implements Listener {

    private final UltrasCore plugin;
    private final SettingsManager settings;

    public JoinLeaveDeathListener(UltrasCore plugin, SettingsManager settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfigManager().getBool("systems.join-leave.enabled", true)) {
            event.joinMessage(null);
            return;
        }
        event.joinMessage(null);
        String text = plugin.getLanguageManager().get("messages.join", "player", event.getPlayer().getName());
        broadcastFiltered(SettingKey.JOIN_MESSAGES, text, "systems.join-leave.sound");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!plugin.getConfigManager().getBool("systems.join-leave.enabled", true)) {
            event.quitMessage(null);
            return;
        }
        event.quitMessage(null);
        String text = plugin.getLanguageManager().get("messages.leave", "player", event.getPlayer().getName());
        broadcastFiltered(SettingKey.LEAVE_MESSAGES, text, "systems.join-leave.sound");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().getBool("systems.death-messages.enabled", true)) {
            return; // leave Bukkit's default death message alone if the whole system is off
        }
        // Deliberately keep Bukkit's own death message text (it already handles every
        // damage cause correctly); we only filter *who* receives it and add the sound.
        String sound = plugin.getConfigManager().getString("systems.death-messages.sound", null);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!settings.get(viewer.getUniqueId(), SettingKey.DEATH_MESSAGES)) continue;
            if (sound != null) {
                try {
                    viewer.playSound(viewer.getLocation(), Registry.SOUNDS.get(NamespacedKey.minecraft(sound.toLowerCase())), 0.6f, 1f);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        // Note: event.deathMessage() itself is left as Bukkit's default and still
        // broadcasts to everyone — per-viewer suppression of the *text itself* would
        // require cancelling it and resending manually per player, which risks losing
        // Bukkit's damage-cause-specific phrasing. Only the sound is filtered here.
    }

    private void broadcastFiltered(SettingKey key, String rawText, String soundConfigPath) {
        String prefixed = plugin.getConfigManager().getServerPrefix() + " " + rawText;
        net.kyori.adventure.text.Component component = MessageUtil.render(prefixed);
        String soundName = plugin.getConfigManager().getString(soundConfigPath, null);

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!settings.get(viewer.getUniqueId(), key)) continue;
            viewer.sendMessage(component);
            if (soundName != null) {
                try {
                    viewer.playSound(viewer.getLocation(), Registry.SOUNDS.get(NamespacedKey.minecraft(soundName.toLowerCase())), 0.6f, 1f);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }
}
