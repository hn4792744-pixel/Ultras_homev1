package com.ultrascore.announcements;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BroadcastCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final AnnouncementManager manager;

    public BroadcastCommand(UltrasCore plugin, AnnouncementManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("bc")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!sender.hasPermission("ultrascore.broadcast")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }
        if (args.length < 1) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("broadcast.usage"));
            return true;
        }

        String message = String.join(" ", args);
        List<String> locations = plugin.getConfigManager().raw().getStringList("systems.broadcast.default-locations");
        if (locations.isEmpty()) locations = List.of("CHAT");
        int duration = plugin.getConfigManager().getInt("systems.broadcast.default-duration-seconds", 3);
        String sound = plugin.getConfigManager().getString("systems.broadcast.sound", null);

        manager.send(Bukkit.getOnlinePlayers(), message, locations, duration, sound);
        return true;
    }
}
