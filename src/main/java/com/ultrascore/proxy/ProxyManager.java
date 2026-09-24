package com.ultrascore.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.ultrascore.core.UltrasCore;
import org.bukkit.entity.Player;

/**
 * A UltrasCore-on-Paper plugin cannot move a player to a different backend
 * server by itself — that's the proxy's job. What it CAN do, and does here, is
 * ask the proxy to do it, using the "BungeeCord" plugin-messaging channel that
 * both BungeeCord and Velocity (with legacy-forwarding compatibility enabled)
 * understand. No Velocity/BungeeCord-specific API or dependency is needed.
 */
public class ProxyManager {

    private final UltrasCore plugin;
    private boolean channelRegistered = false;

    public ProxyManager(UltrasCore plugin) {
        this.plugin = plugin;
    }

    public void registerChannel() {
        if (channelRegistered) return;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        channelRegistered = true;
    }

    public void unregisterChannel() {
        if (!channelRegistered) return;
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, "BungeeCord");
        channelRegistered = false;
    }

    /** Asks the proxy to connect {@code player} to the backend server named {@code serverName}. */
    public void sendToServer(Player player, String serverName) {
        registerChannel();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    public boolean isEnabled() {
        return plugin.getConfigManager().getBool("systems.proxy.enabled", false);
    }
}
