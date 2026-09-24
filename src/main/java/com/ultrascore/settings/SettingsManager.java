package com.ultrascore.settings;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.storage.StorageManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.UUID;

public class SettingsManager {

    private static final String CATEGORY = "settings";

    private final UltrasCore plugin;
    private final StorageManager storage;

    public SettingsManager(UltrasCore plugin, StorageManager storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public boolean get(UUID uuid, SettingKey key) {
        YamlConfiguration doc = storage.get(CATEGORY, uuid);
        return doc.getBoolean(key.getKey(), key.getDefault());
    }

    public void set(UUID uuid, SettingKey key, boolean value) {
        YamlConfiguration doc = storage.get(CATEGORY, uuid);
        doc.set(key.getKey(), value);
        storage.markDirtyAndSaveAsync(CATEGORY, uuid);
    }

    public boolean toggle(UUID uuid, SettingKey key) {
        boolean next = !get(uuid, key);
        set(uuid, key, next);
        return next;
    }
}
