package com.ultrascore.settings;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SettingsCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final SettingsManager settingsManager;
    private final CustomGuiManager customGuiManager;

    public SettingsCommand(UltrasCore plugin, SettingsManager settingsManager, CustomGuiManager customGuiManager) {
        this.plugin = plugin;
        this.settingsManager = settingsManager;
        this.customGuiManager = customGuiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("setting")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.settings.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("ultrascore.admin")) {
                MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
                return true;
            }
            customGuiManager.load();
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.reload-success"));
            return true;
        }

        if (args.length >= 1) {
            if (customGuiManager.exists(args[0])) {
                customGuiManager.open(player, args[0]);
            } else {
                plugin.getGuiManager().open(player, new SettingsGui(plugin, settingsManager, player));
            }
            return true;
        }

        // No args: prefer the "main" custom menu if one exists, otherwise the built-in toggle list.
        if (customGuiManager.exists("main")) {
            customGuiManager.open(player, "main");
        } else {
            plugin.getGuiManager().open(player, new SettingsGui(plugin, settingsManager, player));
        }
        return true;
    }
}
