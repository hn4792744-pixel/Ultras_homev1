package com.ultrascore.tpa;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPACommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final TPAManager manager;

    public TPACommand(UltrasCore plugin, TPAManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("tpa")) {
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
            plugin.getGuiManager().open(player, new TPATargetGui(plugin, manager, player, TPARequest.Type.TO));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "accept" -> manager.accept(player);
            case "deny" -> manager.deny(player);
            case "cancel" -> manager.cancel(player);
            case "toggle" -> manager.toggleReceiving(player);
            case "gui" -> plugin.getGuiManager().open(player, new TPATargetGui(plugin, manager, player, TPARequest.Type.TO));
            case "all" -> {
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (!target.equals(player)) manager.sendRequest(player, target, TPARequest.Type.TO);
                }
            }
            default -> requestByName(player, args[0]);
        }
        return true;
    }

    private void requestByName(Player requester, String name) {
        if (manager.isOnCooldown(requester)) {
            MessageUtil.sendPrefixed(plugin, requester, plugin.getLanguageManager()
                    .get("tpa.cooldown", "seconds", String.valueOf(manager.cooldownRemainingSeconds(requester))));
            return;
        }
        Player target = Bukkit.getPlayerExact(name);
        if (target == null || !target.isOnline() || target.equals(requester)) {
            MessageUtil.sendPrefixed(plugin, requester, plugin.getLanguageManager().get("general.player-not-found"));
            return;
        }
        manager.sendRequest(requester, target, TPARequest.Type.TO);
    }
}
