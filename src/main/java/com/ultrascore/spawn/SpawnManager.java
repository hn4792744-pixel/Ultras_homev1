package com.ultrascore.spawn;

import com.ultrascore.core.UltrasCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * A single global spawn point, stored in its own small file (spawn.yml) rather
 * than through the per-player StorageManager, since it's server-wide state.
 */
public class SpawnManager {

    private final UltrasCore plugin;
    private final File file;
    private YamlConfiguration doc;

    public SpawnManager(UltrasCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "spawn.yml");
        load();
    }

    private void load() {
        if (file.exists()) {
            this.doc = YamlConfiguration.loadConfiguration(file);
        } else {
            this.doc = new YamlConfiguration();
        }
    }

    public boolean isSet() {
        return doc.contains("world");
    }

    public Location getSpawn() {
        if (!isSet()) return null;
        World world = Bukkit.getWorld(doc.getString("world"));
        if (world == null) return null;
        return new Location(world, doc.getDouble("x"), doc.getDouble("y"), doc.getDouble("z"),
                (float) doc.getDouble("yaw"), (float) doc.getDouble("pitch"));
    }

    public void setSpawn(Location location) {
        doc.set("world", location.getWorld().getName());
        doc.set("x", location.getX());
        doc.set("y", location.getY());
        doc.set("z", location.getZ());
        doc.set("yaw", (double) location.getYaw());
        doc.set("pitch", (double) location.getPitch());
        saveAsync();
    }

    private void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                doc.save(file);
            } catch (IOException e) {
                plugin.getLogManager().error("spawn", "Failed to save spawn.yml: " + e.getMessage());
            }
        });
    }
}
