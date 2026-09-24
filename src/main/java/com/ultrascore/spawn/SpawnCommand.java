package com.ultrascore.spawn;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final SpawnManager manager;

    public SpawnCommand(UltrasCore plugin, SpawnManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("spawn")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.spawn.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }
        Location spawn = manager.getSpawn();
        if (spawn == null) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("spawn.not-set"));
            return true;
        }
        player.teleport(spawn);
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("spawn.teleported"));
        return true;
    }
}
