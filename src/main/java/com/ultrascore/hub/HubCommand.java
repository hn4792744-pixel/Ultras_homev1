package com.ultrascore.hub;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.proxy.ProxyManager;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HubCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final HubManager manager;
    private final ProxyManager proxyManager;

    public HubCommand(UltrasCore plugin, HubManager manager, ProxyManager proxyManager) {
        this.plugin = plugin;
        this.manager = manager;
        this.proxyManager = proxyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("hub")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }
        if (!player.hasPermission("ultrascore.hub.use")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }

        if (manager.isProxyMode()) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("hub.teleporting-proxy"));
            proxyManager.sendToServer(player, manager.proxyServerName());
            return true;
        }

        Location hub = manager.getHub();
        if (hub == null) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("hub.not-set"));
            return true;
        }

        int delay = plugin.getConfigManager().getInt("systems.hub.teleport-delay-seconds", 0);
        if (delay <= 0 || player.hasPermission("ultrascore.tpa.bypass")) {
            player.teleport(hub);
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("hub.teleporting"));
            return true;
        }

        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("hub.teleporting"));
        plugin.getTeleportCountdown().start(player, () -> hub, delay, true, null, null, p -> {});
        return true;
    }
}
