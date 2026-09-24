package com.ultrascore.visibility;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HideCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final VisibilityManager manager;

    public HideCommand(UltrasCore plugin, VisibilityManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("hide")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.visibility.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }

        boolean hiding;
        switch (label.toLowerCase()) {
            case "hideplayers" -> hiding = true;
            case "showplayers" -> hiding = false;
            default -> hiding = !manager.isHiding(player); // "hide" = toggle
        }

        manager.setHiding(player, hiding);
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager()
                .get(hiding ? "visibility.hiding" : "visibility.showing"));
        return true;
    }
}
