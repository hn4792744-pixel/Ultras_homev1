package com.ultrascore.homes;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeAdminCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final HomeManager manager;

    public HomeAdminCommand(UltrasCore plugin, HomeManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.homes.admin")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }
        if (args.length < 1) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager()
                    .get("general.invalid-args", "usage", "/home_admin <player>"));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target.getName() == null && !target.hasPlayedBefore()) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.player-not-found"));
            return true;
        }
        plugin.getGuiManager().open(player, new HomesGui(plugin, manager, player, target.getUniqueId()));
        return true;
    }
}
