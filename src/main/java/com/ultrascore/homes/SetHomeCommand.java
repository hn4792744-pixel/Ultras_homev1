package com.ultrascore.homes;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final HomeManager manager;

    public SetHomeCommand(UltrasCore plugin, HomeManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("sethome")) {
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

        String name = args.length >= 1 ? args[0] : "home";
        boolean ok = manager.setHome(player, name, player.getLocation());
        if (ok) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("homes.set", "name", name));
        } else {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager()
                    .get("homes.limit-reached", "limit", String.valueOf(manager.getLimit(player))));
        }
        return true;
    }
}
