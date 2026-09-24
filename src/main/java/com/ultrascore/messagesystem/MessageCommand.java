package com.ultrascore.messagesystem;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.settings.SettingKey;
import com.ultrascore.settings.SettingsManager;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final SettingsManager settings;

    public MessageCommand(UltrasCore plugin, SettingsManager settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("msg")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.pm.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("toggle")) {
            boolean next = settings.toggle(player.getUniqueId(), SettingKey.PRIVATE_MESSAGES);
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager()
                    .get(next ? "pm.toggled-on" : "pm.toggled-off"));
            return true;
        }

        if (args.length < 2) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("pm.no-target"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.player-not-found"));
            return true;
        }
        if (target.equals(player)) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("pm.self"));
            return true;
        }
        if (!settings.get(target.getUniqueId(), SettingKey.PRIVATE_MESSAGES)) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("pm.target-disabled"));
            return true;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        MessageUtil.send(player, plugin.getLanguageManager().get("pm.sent", "player", target.getName(), "message", message));
        MessageUtil.send(target, plugin.getLanguageManager().get("pm.received", "player", player.getName(), "message", message));
        return true;
    }
}
