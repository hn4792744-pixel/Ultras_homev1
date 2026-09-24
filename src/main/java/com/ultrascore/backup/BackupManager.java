package com.ultrascore.backup;

import com.ultrascore.core.UltrasCore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

/** Copies config.yml/warps.yml/spawn.yml/hub.yml into backups/ on every /ultrascore reload (see section 45). */
public class BackupManager {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final String[] FILES_TO_BACKUP = {"config.yml", "warps.yml", "spawn.yml", "hub.yml"};

    private final UltrasCore plugin;
    private final File backupsDir;

    public BackupManager(UltrasCore plugin) {
        this.plugin = plugin;
        this.backupsDir = new File(plugin.getDataFolder(), "backups");
    }

    public void backupNow() {
        if (!plugin.getConfigManager().getBool("systems.backups.enabled", true)) return;
        backupsDir.mkdirs();
        String stamp = java.time.LocalDateTime.now().format(STAMP);

        for (String name : FILES_TO_BACKUP) {
            File source = new File(plugin.getDataFolder(), name);
            if (!source.exists()) continue;
            try {
                File dest = new File(backupsDir, name + "." + stamp + ".bak");
                Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                prune(name);
            } catch (IOException e) {
                plugin.getLogManager().error("core", "Backup of " + name + " failed: " + e.getMessage());
            }
        }
    }

    private void prune(String baseName) {
        int keep = plugin.getConfigManager().getInt("systems.backups.keep", 10);
        File[] matches = backupsDir.listFiles((dir, n) -> n.startsWith(baseName + "."));
        if (matches == null || matches.length <= keep) return;

        Arrays.sort(matches, Comparator.comparingLong(File::lastModified));
        int toDelete = matches.length - keep;
        for (int i = 0; i < toDelete; i++) {
            matches[i].delete();
        }
    }
}
