package com.ultrascore.nightvision;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NightVisionCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final NightVisionManager manager;

    public NightVisionCommand(UltrasCore plugin, NightVisionManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("nv")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.nightvision.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }

        boolean on;
        if (args.length >= 1 && args[0].equalsIgnoreCase("on")) {
            manager.setOn(player, true);
            on = true;
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("off")) {
            manager.setOn(player, false);
            on = false;
        } else {
            on = manager.toggle(player);
        }

        player.sendActionBar(MessageUtil.render(plugin.getLanguageManager().get(on ? "nightvision.on" : "nightvision.off")));
        return true;
    }
}
