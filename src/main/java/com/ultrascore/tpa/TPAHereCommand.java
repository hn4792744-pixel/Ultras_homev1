package com.ultrascore.tpa;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPAHereCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final TPAManager manager;

    public TPAHereCommand(UltrasCore plugin, TPAManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("tpahere")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.tpa.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            plugin.getGuiManager().open(player, new TPATargetGui(plugin, manager, player, TPARequest.Type.HERE));
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (!target.equals(player)) manager.sendRequest(player, target, TPARequest.Type.HERE);
            }
            return true;
        }

        if (manager.isOnCooldown(player)) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager()
                    .get("tpa.cooldown", "seconds", String.valueOf(manager.cooldownRemainingSeconds(player))));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline() || target.equals(player)) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.player-not-found"));
            return true;
        }
        manager.sendRequest(player, target, TPARequest.Type.HERE);
        return true;
    }
}
