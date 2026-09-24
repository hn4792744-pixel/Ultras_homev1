package com.ultrascore.hub;

import com.ultrascore.core.UltrasCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class HubManager {

    private final UltrasCore plugin;
    private final File file;
    private YamlConfiguration doc;

    public HubManager(UltrasCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "hub.yml");
        load();
    }

    private void load() {
        this.doc = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
    }

    public boolean isSet() {
        return doc.contains("world");
    }

    public Location getHub() {
        if (!isSet()) return null;
        World world = Bukkit.getWorld(doc.getString("world"));
        if (world == null) return null;
        return new Location(world, doc.getDouble("x"), doc.getDouble("y"), doc.getDouble("z"),
                (float) doc.getDouble("yaw"), (float) doc.getDouble("pitch"));
    }

    public void setHub(Location location) {
        doc.set("world", location.getWorld().getName());
        doc.set("x", location.getX());
        doc.set("y", location.getY());
        doc.set("z", location.getZ());
        doc.set("yaw", (double) location.getYaw());
        doc.set("pitch", (double) location.getPitch());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                doc.save(file);
            } catch (IOException e) {
                plugin.getLogManager().error("hub", "Failed to save hub.yml: " + e.getMessage());
            }
        });
    }

    public boolean isProxyMode() {
        return "PROXY".equalsIgnoreCase(plugin.getConfigManager().getString("systems.hub.mode", "LOCAL"));
    }

    public String proxyServerName() {
        return plugin.getConfigManager().getString("systems.hub.proxy-server-name", "hub");
    }
}
