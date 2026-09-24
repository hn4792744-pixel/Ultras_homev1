package com.ultrascore.logs;

import com.ultrascore.core.UltrasCore;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Writes to both the console and a per-system rotating-by-day file under
 * plugins/UltrasCore/logs/<system>/. Each system's file logging can be disabled
 * independently via config (logs.<system>: false) without touching console output
 * of warnings/errors, which always show.
 */
public class LogManager {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final UltrasCore plugin;
    private final Logger bukkitLogger;
    private final Path logsDir;

    public LogManager(UltrasCore plugin) {
        this.plugin = plugin;
        this.bukkitLogger = plugin.getLogger();
        this.logsDir = plugin.getDataFolder().toPath().resolve("logs");
        try {
            Files.createDirectories(logsDir);
        } catch (IOException ignored) {
            // Non-fatal: file logging degrades to console-only.
        }
    }

    public void info(String system, String message) {
        bukkitLogger.info("[" + system + "] " + message);
        writeToFile(system, "INFO", message);
    }

    public void warn(String system, String message) {
        bukkitLogger.warning("[" + system + "] " + message);
        writeToFile(system, "WARN", message);
    }

    public void error(String system, String message) {
        bukkitLogger.severe("[" + system + "] " + message);
        writeToFile(system, "ERROR", message);
    }

    public void debug(String system, String message) {
        if (!plugin.getConfigManager().isDebug()) return;
        bukkitLogger.info("[DEBUG:" + system + "] " + message);
        writeToFile(system, "DEBUG", message);
    }

    private void writeToFile(String system, String level, String message) {
        if (!plugin.getConfigManager().isLogEnabled(system)) return;
        try {
            Path systemDir = logsDir.resolve(system);
            Files.createDirectories(systemDir);
            String date = java.time.LocalDate.now().toString();
            Path file = systemDir.resolve(date + ".log");
            try (FileWriter fw = new FileWriter(file.toFile(), true)) {
                fw.write("[" + LocalDateTime.now().format(TS) + "] [" + level + "] " + message + System.lineSeparator());
            }
        } catch (IOException e) {
            // Never let logging I/O break gameplay; surface once to console instead.
            bukkitLogger.warning("[logs] Failed to write log file for '" + system + "': " + e.getMessage());
        }
    }
}
