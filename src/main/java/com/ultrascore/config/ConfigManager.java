package com.ultrascore.config;

import com.ultrascore.core.UltrasCore;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Thin, defensive wrapper around config.yml. Every getter falls back to a sane
 * default so a missing or malformed key never throws mid-gameplay.
 */
public class ConfigManager {

    private final UltrasCore plugin;
    private FileConfiguration cfg;

    public ConfigManager(UltrasCore plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfig();
    }

    public void reload() {
        this.cfg = plugin.getConfig();
    }

    public FileConfiguration raw() {
        return cfg;
    }

    public String getLanguage() {
        return cfg.getString("language", "en");
    }

    public String getServerName() {
        return cfg.getString("server.name", "ULTRAS MC");
    }

    public String getServerPrefix() {
        return cfg.getString("server.prefix", "<red>ULTRAS</red> <dark_gray>|</dark_gray>");
    }

    public boolean isDebug() {
        return cfg.getBoolean("debug", false);
    }

    public String getStorageType() {
        return cfg.getString("storage.type", "YAML");
    }

    public boolean isLogEnabled(String system) {
        return cfg.getBoolean("logs.enabled", true) && cfg.getBoolean("logs." + system, true);
    }

    public boolean isSystemEnabled(String system) {
        return cfg.getBoolean("systems." + system + ".enabled", false);
    }

    public boolean isCommandEnabled(String command) {
        return cfg.getBoolean("commands." + command, true);
    }

    public int getInt(String path, int def) {
        return cfg.getInt(path, def);
    }

    public boolean getBool(String path, boolean def) {
        return cfg.getBoolean(path, def);
    }

    public String getString(String path, String def) {
        return cfg.getString(path, def);
    }

    /**
     * Logs warnings for obviously-invalid values instead of failing silently or
     * crashing a system at runtime — every reader already falls back to a safe
     * default regardless, so this is purely diagnostic (section 44/50 "final
     * validation" from the spec, minus the parts that need a real `mvn` build).
     */
    public void validate() {
        checkPositive("systems.tpa.teleport-delay-seconds");
        checkPositive("systems.tpa.cooldown-seconds");
        checkPositive("systems.homes.default-limit");
        checkPositive("systems.homes.op-limit");
        checkPositive("systems.rtp.attempts");

        int rtpMin = getInt("systems.rtp.min-radius", 0);
        int rtpMax = getInt("systems.rtp.max-radius", 0);
        if (rtpMin >= rtpMax) {
            plugin.getLogManager().warn("core", "systems.rtp.min-radius (" + rtpMin
                    + ") should be smaller than max-radius (" + rtpMax + ") — RTP will clamp this at runtime, but fix it in config.yml.");
        }

        String lang = getLanguage();
        if (!lang.equals("en") && !lang.equals("ar") && plugin.getResource("lang/" + lang + ".yml") == null) {
            plugin.getLogManager().warn("core", "language: '" + lang
                    + "' has no bundled lang/" + lang + ".yml and no override file was found — falling back to English.");
        }

        String hubMode = getString("systems.hub.mode", "LOCAL");
        if (!hubMode.equalsIgnoreCase("LOCAL") && !hubMode.equalsIgnoreCase("PROXY")) {
            plugin.getLogManager().warn("core", "systems.hub.mode: '" + hubMode + "' is neither LOCAL nor PROXY — treating it as LOCAL.");
        }
    }

    private void checkPositive(String path) {
        int value = getInt(path, 0);
        if (value < 0) {
            plugin.getLogManager().warn("core", path + " is negative (" + value + ") in config.yml — this is almost certainly a typo.");
        }
    }
}
