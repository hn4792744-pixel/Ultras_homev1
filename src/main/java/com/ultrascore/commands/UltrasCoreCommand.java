package com.ultrascore.commands;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UltrasCoreCommand implements CommandExecutor {

    private final UltrasCore plugin;

    public UltrasCoreCommand(UltrasCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultrascore.admin")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            MessageUtil.send(sender, "<gradient:#ff0000:#8a0000><bold>UltrasCore</bold></gradient> <gray>v" + plugin.getPluginMeta().getVersion());
            MessageUtil.send(sender, "<gray>/tpa, /tpahere <white>— teleport requests");
            MessageUtil.send(sender, "<gray>/home, /homes, /sethome, /delhome <white>— homes");
            MessageUtil.send(sender, "<gray>/home_admin, /uc_home <white>— homes administration");
            MessageUtil.send(sender, "<gray>/ultrascore reload <white>— reload config, language and systems");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            try {
                plugin.reloadUltrasCore();
                MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.reload-success"));
            } catch (Exception e) {
                plugin.getLogManager().error("core", "Reload failed: " + e.getMessage());
                MessageUtil.sendPrefixed(plugin, sender, "<red>Reload failed — see console for details.");
            }
            return true;
        }

        return true;
    }
}
