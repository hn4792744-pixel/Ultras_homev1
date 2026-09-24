package com.ultrascore.placeholders;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.settings.SettingKey;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
 * Registered only if PlaceholderAPI is present (see UltrasCore#registerSystems).
 * All identifiers are documented in the original spec section 31; a few that
 * depend on systems not yet built (warps count is covered, but e.g. a
 * per-warp placeholder isn't) are simply left out rather than faked.
 */
public class UltrasCorePlaceholders extends PlaceholderExpansion {

    private final UltrasCore plugin;

    public UltrasCorePlaceholders(UltrasCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "ultrascore";
    }

    @Override
    public String getAuthor() {
        return "Hussein";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // survives /papi reload
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equals("server_name")) {
            return plugin.getConfigManager().getServerName();
        }

        if (player == null) return "";

        return switch (identifier) {
            case "player" -> player.getName();
            case "uuid" -> player.getUniqueId().toString();
            case "world" -> player.getWorld().getName();
            case "x" -> String.valueOf(player.getLocation().getBlockX());
            case "y" -> String.valueOf(player.getLocation().getBlockY());
            case "z" -> String.valueOf(player.getLocation().getBlockZ());
            case "ping" -> String.valueOf(player.getPing());
            case "homes" -> plugin.getHomeManager() != null
                    ? String.valueOf(plugin.getHomeManager().getHomes(player.getUniqueId()).size()) : "0";
            case "home_limit" -> plugin.getHomeManager() != null
                    ? String.valueOf(plugin.getHomeManager().getLimit(player)) : "0";
            case "warps" -> plugin.getWarpManager() != null ? String.valueOf(plugin.getWarpManager().all().size()) : "0";
            case "tpa_status" -> plugin.getTpaManager() != null
                    ? (plugin.getTpaManager().isReceiving(player.getUniqueId()) ? "on" : "off") : "off";
            case "chat_status" -> boolStatus(player, SettingKey.CHAT);
            case "pm_status" -> boolStatus(player, SettingKey.PRIVATE_MESSAGES);
            case "nv_status" -> boolStatus(player, SettingKey.NIGHT_VISION);
            default -> null; // unknown identifier — let PlaceholderAPI show its own "not found" text
        };
    }

    private String boolStatus(Player player, SettingKey key) {
        if (plugin.getSettingsManager() == null) return "off";
        return plugin.getSettingsManager().get(player.getUniqueId(), key) ? "on" : "off";
    }
}
