package com.ultrascore.playernames;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.settings.SettingKey;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NamesCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final NamesManager manager;

    public NamesCommand(UltrasCore plugin, NamesManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("names")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.names.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }

        boolean shown;
        if (args.length >= 1 && args[0].equalsIgnoreCase("on")) {
            plugin.getSettingsManager().set(player.getUniqueId(), SettingKey.NAMES, true);
            manager.applyFor(player);
            shown = true;
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("off")) {
            plugin.getSettingsManager().set(player.getUniqueId(), SettingKey.NAMES, false);
            manager.applyFor(player);
            shown = false;
        } else {
            shown = manager.toggleNames(player);
        }

        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get(shown ? "names.shown" : "names.hidden"));
        return true;
    }
}
