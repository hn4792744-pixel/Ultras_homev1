package com.ultrascore.hub;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHubCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final HubManager manager;

    public SetHubCommand(UltrasCore plugin, HubManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.hub.admin")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }
        manager.setHub(player.getLocation());
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("hub.set"));
        return true;
    }
}
