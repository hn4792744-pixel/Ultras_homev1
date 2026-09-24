package com.ultrascore.homes;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final HomeManager manager;

    public DelHomeCommand(UltrasCore plugin, HomeManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("delhome")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.homes.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }
        if (args.length < 1) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager()
                    .get("general.invalid-args", "usage", "/delhome <name>"));
            return true;
        }

        boolean ok = manager.deleteHome(player.getUniqueId(), args[0]);
        if (ok) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("homes.deleted", "name", args[0]));
        } else {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("homes.not-found", "name", args[0]));
        }
        return true;
    }
}
