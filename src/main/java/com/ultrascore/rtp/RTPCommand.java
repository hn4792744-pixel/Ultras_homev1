package com.ultrascore.rtp;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTPCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final RTPManager manager;

    public RTPCommand(UltrasCore plugin, RTPManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("rtp")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ultrascore.admin")) {
                MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.no-permission"));
                return true;
            }
            plugin.reloadConfig();
            plugin.getConfigManager().reload();
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("rtp.reload-success"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.rtp.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }
        if (!manager.isCommandWorldAllowed(player.getWorld().getName())) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("rtp.command-world-not-allowed"));
            return true;
        }
        if (manager.isOnCooldown(player)) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager()
                    .get("rtp.cooldown", "seconds", String.valueOf(manager.cooldownRemainingSeconds(player))));
            return true;
        }

        World world = player.getWorld();
        if (args.length >= 1) {
            World named = Bukkit.getWorld(args[0]);
            if (named == null) {
                MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.player-not-found"));
                return true;
            }
            world = named;
        }

        manager.teleportRandomly(player, world);
        return true;
    }
}
