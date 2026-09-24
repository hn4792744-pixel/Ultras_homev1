package com.ultrascore.settings;

import com.ultrascore.core.UltrasCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lets Hussein add new GUIs by dropping a YAML file in plugins/UltrasCore/settings/,
 * without touching Java code. One file = one GUI, id = file name without ".yml".
 * See settings/main.yml (shipped as an example) for the schema.
 */
public class CustomGuiManager {

    private final UltrasCore plugin;
    private final File folder;
    private final Map<String, CustomGuiDefinition> definitions = new LinkedHashMap<>();

    public CustomGuiManager(UltrasCore plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "settings");
        if (!folder.exists()) {
            folder.mkdirs();
            plugin.saveResource("settings/main.yml", false);
        }
        load();
    }

    public void load() {
        definitions.clear();
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null) return;
        for (File file : files) {
            String id = file.getName().substring(0, file.getName().length() - 4);
            try {
                definitions.put(id.toLowerCase(), parse(id, file));
            } catch (Exception e) {
                plugin.getLogManager().error("gui", "Skipping invalid custom GUI file '" + file.getName()
                        + "': " + e.getMessage());
            }
        }
    }

    private CustomGuiDefinition parse(String id, File file) {
        YamlConfiguration doc = YamlConfiguration.loadConfiguration(file);
        CustomGuiDefinition def = new CustomGuiDefinition(id);

        ConfigurationSection guiSection = doc.getConfigurationSection("gui");
        if (guiSection != null) {
            def.title = guiSection.getString("title", def.title);
            def.size = clampSize(guiSection.getInt("size", def.size));
        }

        ConfigurationSection buttons = doc.getConfigurationSection("buttons");
        if (buttons != null) {
            for (String key : buttons.getKeys(false)) {
                ConfigurationSection b = buttons.getConfigurationSection(key);
                if (b == null) continue;
                CustomGuiButton btn = new CustomGuiButton();
                btn.slot = b.getInt("slot", 0);
                btn.material = b.getString("material", "STONE");
                btn.name = b.getString("name", key);
                btn.lore = b.getStringList("lore");
                btn.permission = b.getString("permission", null);
                btn.enabled = b.getBoolean("enabled", true);
                btn.command = b.getString("command", null);
                btn.action = b.getString("action", null);
                btn.targetGui = b.getString("target-gui", null);
                btn.sound = b.getString("sound", null);
                if (btn.enabled) {
                    def.buttons.add(btn);
                }
            }
        }
        return def;
    }

    private int clampSize(int size) {
        int rounded = Math.max(9, Math.min(54, size));
        return rounded - (rounded % 9 == 0 ? 0 : rounded % 9);
    }

    public boolean exists(String id) {
        return definitions.containsKey(id.toLowerCase());
    }

    public void open(Player player, String id) {
        CustomGuiDefinition def = definitions.get(id.toLowerCase());
        if (def == null) return;
        plugin.getGuiManager().open(player, new CustomGui(plugin, this, def, player));
    }
}
