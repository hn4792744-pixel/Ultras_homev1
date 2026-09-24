package com.ultrascore.playernames;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.settings.SettingKey;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Stores the player's rank-prefix-visibility preference (readable via a future
 * PlaceholderAPI expansion or an external rank plugin). UltrasCore doesn't own
 * rank prefixes itself, so this toggle doesn't render anything on its own here —
 * see README "Known simplifications" for what a rank/prefix plugin needs to check.
 */
public class RanksCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final NamesManager namesManager;

    public RanksCommand(UltrasCore plugin, NamesManager namesManager) {
        this.plugin = plugin;
        this.namesManager = namesManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("ranks")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.ranks.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }
        if (!namesManager.namesShown(player)) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("ranks.blocked-by-names"));
            return true;
        }

        boolean shown;
        if (args.length >= 1 && args[0].equalsIgnoreCase("on")) {
            plugin.getSettingsManager().set(player.getUniqueId(), SettingKey.RANKS, true);
            shown = true;
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("off")) {
            plugin.getSettingsManager().set(player.getUniqueId(), SettingKey.RANKS, false);
            shown = false;
        } else {
            shown = plugin.getSettingsManager().toggle(player.getUniqueId(), SettingKey.RANKS);
        }

        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get(shown ? "ranks.shown" : "ranks.hidden"));
        return true;
    }
}
