package com.ultrascore.tpa;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TPAAdminCommand implements CommandExecutor {

    private final UltrasCore plugin;

    public TPAAdminCommand(UltrasCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ultrascore.admin")) {
                MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.no-permission"));
                return true;
            }
            plugin.reloadConfig();
            plugin.getConfigManager().reload();
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.reload-success"));
            return true;
        }

        MessageUtil.send(sender, "<gradient:#ff0000:#8a0000><bold>TPA — Help</bold></gradient>");
        MessageUtil.send(sender, "<gray>/tpa <player> <white>— send a teleport-to-them request");
        MessageUtil.send(sender, "<gray>/tpahere <player> <white>— ask them to teleport to you");
        MessageUtil.send(sender, "<gray>/tpa accept|deny|cancel <white>— manage the pending request");
        MessageUtil.send(sender, "<gray>/tpa toggle <white>— stop/start receiving requests");
        MessageUtil.send(sender, "<gray>/tpa all , /tpahere all <white>— request everyone online");
        return true;
    }
}
