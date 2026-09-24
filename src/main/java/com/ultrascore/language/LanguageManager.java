package com.ultrascore.language;

import com.ultrascore.core.UltrasCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads lang/en.yml and lang/ar.yml from the jar (and, if present, user overrides
 * on disk under plugins/UltrasCore/lang/). All in-game player-facing text lives
 * here; Java code should never hardcode a message string.
 */
public class LanguageManager {

    private final UltrasCore plugin;
    private YamlConfiguration active;
    private YamlConfiguration fallback; // english, used if a key is missing from the active language

    public LanguageManager(UltrasCore plugin) {
        this.plugin = plugin;
        load();
    }

    public void reload() {
        load();
    }

    private void load() {
        this.fallback = loadBundled("en.yml");
        String lang = plugin.getConfigManager().getLanguage();
        File external = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (external.exists()) {
            this.active = YamlConfiguration.loadConfiguration(external);
        } else {
            YamlConfiguration bundled = loadBundled(lang + ".yml");
            this.active = bundled != null ? bundled : fallback;
        }
        if (this.active == null) {
            this.active = fallback;
        }
    }

    private YamlConfiguration loadBundled(String fileName) {
        try (InputStream in = plugin.getResource("lang/" + fileName)) {
            if (in == null) return null;
            return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (Exception e) {
            plugin.getLogManager().error("language", "Failed to load lang/" + fileName + ": " + e.getMessage());
            return null;
        }
    }

    /** Raw MiniMessage string for a key, e.g. "tpa.sent", with {@code {placeholders}} intact. */
    public String raw(String key) {
        String value = active.getString(key);
        if (value == null) {
            value = fallback.getString(key);
        }
        return value != null ? value : "<red>Missing message: " + key;
    }

    /** Resolves a key and substitutes {placeholder} tokens with the given values (pairs of name, value). */
    public String get(String key, Object... placeholderPairs) {
        String template = raw(key);
        if (placeholderPairs.length == 0) {
            return template;
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i + 1 < placeholderPairs.length; i += 2) {
            map.put(String.valueOf(placeholderPairs[i]), String.valueOf(placeholderPairs[i + 1]));
        }
        String result = template;
        for (Map.Entry<String, String> e : map.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", e.getValue());
        }
        return result;
    }
}
