package com.ultrascore.homes;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class HomeCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final HomeManager manager;

    public HomeCommand(UltrasCore plugin, HomeManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("home")) {
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

        Map<String, Home> homes = manager.getHomes(player.getUniqueId());

        String name;
        if (args.length >= 1) {
            name = args[0];
        } else if (homes.size() == 1) {
            name = homes.keySet().iterator().next();
        } else {
            plugin.getGuiManager().open(player, new HomesGui(plugin, manager, player, player.getUniqueId()));
            return true;
        }

        manager.teleportToHome(player, name);
        return true;
    }
}
