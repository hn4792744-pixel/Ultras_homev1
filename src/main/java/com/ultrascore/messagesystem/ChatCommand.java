package com.ultrascore.messagesystem;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.settings.SettingKey;
import com.ultrascore.settings.SettingsManager;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final SettingsManager settings;

    public ChatCommand(UltrasCore plugin, SettingsManager settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("chat")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.chat.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }

        boolean next = settings.toggle(player.getUniqueId(), SettingKey.CHAT);
        String key = next ? "chat.unmuted" : "chat.muted";
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get(key));
        return true;
    }
}
