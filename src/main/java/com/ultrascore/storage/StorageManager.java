package com.ultrascore.storage;

import com.ultrascore.core.UltrasCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic per-player YAML storage keyed by (category, UUID), e.g. category
 * "homes" -> plugins/UltrasCore/playerdata/homes/<uuid>.yml.
 *
 * Reads happen on-demand and are cached in memory; writes are queued and
 * flushed asynchronously so gameplay never blocks on disk I/O. onDisable()
 * flushes everything synchronously as a last resort (server is stopping
 * anyway, so a brief blocking write there is acceptable and necessary).
 */
public class StorageManager {

    private final UltrasCore plugin;
    private final File baseDir;
    private final Map<String, YamlConfiguration> cache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> dirty = new ConcurrentHashMap<>();

    public StorageManager(UltrasCore plugin) {
        this.plugin = plugin;
        this.baseDir = new File(plugin.getDataFolder(), "playerdata");
        baseDir.mkdirs();
    }

    private File fileFor(String category, UUID uuid) {
        File dir = new File(baseDir, category);
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, uuid.toString() + ".yml");
    }

    private String cacheKey(String category, UUID uuid) {
        return category + ":" + uuid;
    }

    /** Returns the (possibly cached) YAML document for this player+category, loading from disk if needed. */
    public YamlConfiguration get(String category, UUID uuid) {
        String key = cacheKey(category, uuid);
        return cache.computeIfAbsent(key, k -> {
            File f = fileFor(category, uuid);
            if (f.exists()) {
                try {
                    return YamlConfiguration.loadConfiguration(f);
                } catch (Exception e) {
                    plugin.getLogManager().error("storage", "Corrupt file " + f.getName() + " (" + category
                            + "): " + e.getMessage() + " — starting with an empty document, original left on disk.");
                    return new YamlConfiguration();
                }
            }
            return new YamlConfiguration();
        });
    }

    /** Marks a player's data as changed and schedules an async flush. */
    public void markDirtyAndSaveAsync(String category, UUID uuid) {
        String key = cacheKey(category, uuid);
        dirty.put(key, true);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> flush(category, uuid));
    }

    private void flush(String category, UUID uuid) {
        String key = cacheKey(category, uuid);
        YamlConfiguration doc = cache.get(key);
        if (doc == null) return;
        try {
            doc.save(fileFor(category, uuid));
            dirty.remove(key);
        } catch (IOException e) {
            plugin.getLogManager().error("storage", "Failed to save " + category + "/" + uuid + ": " + e.getMessage());
        }
    }

    /** Synchronously flushes every dirty document. Only meant for onDisable(). */
    public void saveAllSync() {
        for (String key : dirty.keySet()) {
            String[] parts = key.split(":", 2);
            if (parts.length != 2) continue;
            YamlConfiguration doc = cache.get(key);
            if (doc == null) continue;
            try {
                doc.save(fileFor(parts[0], UUID.fromString(parts[1])));
            } catch (Exception e) {
                plugin.getLogManager().error("storage", "Shutdown save failed for " + key + ": " + e.getMessage());
            }
        }
    }
}
