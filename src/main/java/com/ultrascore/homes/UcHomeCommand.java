package com.ultrascore.homes;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UcHomeCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final HomeManager manager;

    public UcHomeCommand(UltrasCore plugin, HomeManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultrascore.homes.admin")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }
        if (args.length < 2) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager()
                    .get("general.invalid-args", "usage", "/uc_home <add|reset|set|list> <player> [amount]"));
            return true;
        }

        String sub = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        switch (sub) {
            case "list" -> {
                var homes = manager.getHomes(target.getUniqueId());
                MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("homes.list-header"));
                for (String name : homes.keySet()) {
                    MessageUtil.send(sender, "<gray> - <white>" + name);
                }
            }
            case "reset" -> {
                manager.resetLimitOverride(target.getUniqueId());
                MessageUtil.sendPrefixed(plugin, sender, "<green>Home limit for " + args[1] + " reset to default.");
            }
            case "set" -> {
                int amount = parseAmount(args);
                manager.setLimitOverride(target.getUniqueId(), amount);
                MessageUtil.sendPrefixed(plugin, sender, "<green>Home limit for " + args[1] + " set to " + amount + ".");
            }
            case "add" -> {
                int amount = parseAmount(args);
                OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
                org.bukkit.entity.Player online = op.getPlayer();
                int current = online != null ? manager.getLimit(online)
                        : plugin.getConfigManager().getInt("systems.homes.default-limit", 3);
                manager.setLimitOverride(target.getUniqueId(), current + amount);
                MessageUtil.sendPrefixed(plugin, sender, "<green>Added " + amount + " home slots to " + args[1] + ".");
            }
            default -> MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager()
                    .get("general.invalid-args", "usage", "/uc_home <add|reset|set|list> <player> [amount]"));
        }
        return true;
    }

    private int parseAmount(String[] args) {
        if (args.length < 3) return 0;
        try {
            return Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
