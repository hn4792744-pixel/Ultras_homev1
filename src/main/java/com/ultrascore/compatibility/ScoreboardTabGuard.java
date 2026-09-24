package com.ultrascore.compatibility;

import com.ultrascore.core.UltrasCore;

/**
 * UltrasCore does not ship its own sidebar-scoreboard or tab-list renderer in
 * this phase (see README) — this class only guards against *future* conflicts:
 * it detects known TAB/scoreboard plugins and logs a clear notice so a config
 * mistake (enabling systems.scoreboard/tab while one of these is present)
 * doesn't silently fight another plugin for the same display.
 */
public final class ScoreboardTabGuard {

    private static final String[] KNOWN_TAB_PLUGINS = {"TAB", "TabList", "TabbedAPI", "FeatherBoard", "ScoreboardStats"};

    private ScoreboardTabGuard() {}

    public static void check(UltrasCore plugin) {
        String found = null;
        for (String name : KNOWN_TAB_PLUGINS) {
            if (plugin.getServer().getPluginManager().getPlugin(name) != null) {
                found = name;
                break;
            }
        }

        boolean scoreboardWanted = plugin.getConfigManager().getBool("systems.scoreboard.enabled", false);
        boolean tabWanted = plugin.getConfigManager().getBool("systems.tab.enable-tab-features", false);
        boolean forceDisable = plugin.getConfigManager().getBool("systems.scoreboard.force-disable-if-tab-plugin", true);

        if (found != null) {
            if ((scoreboardWanted || tabWanted) && forceDisable) {
                plugin.getLogManager().warn("compatibility", "Detected '" + found
                        + "' — systems.scoreboard/tab are enabled in config but will be treated as OFF this "
                        + "session to avoid fighting it over the sidebar/tab list. Set them to false in "
                        + "config.yml to remove this warning, or force-disable-if-tab-plugin: false to override.");
            } else {
                plugin.getLogManager().info("compatibility", "Detected '" + found + "' — UltrasCore's own "
                        + "scoreboard/tab features stay off (as configured).");
            }
        } else if (scoreboardWanted || tabWanted) {
            plugin.getLogManager().info("compatibility", "systems.scoreboard/tab are enabled in config, but "
                    + "UltrasCore doesn't render its own sidebar/tab list yet in this build — no visible effect.");
        }
    }

    /** True if UltrasCore's own scoreboard/tab features should be treated as active right now. */
    public static boolean scoreboardActive(UltrasCore plugin) {
        return effectiveFlag(plugin, "systems.scoreboard.enabled");
    }

    public static boolean tabActive(UltrasCore plugin) {
        return effectiveFlag(plugin, "systems.tab.enable-tab-features");
    }

    private static boolean effectiveFlag(UltrasCore plugin, String path) {
        if (!plugin.getConfigManager().getBool(path, false)) return false;
        boolean forceDisable = plugin.getConfigManager().getBool("systems.scoreboard.force-disable-if-tab-plugin", true);
        if (!forceDisable) return true;
        for (String name : KNOWN_TAB_PLUGINS) {
            if (plugin.getServer().getPluginManager().getPlugin(name) != null) return false;
        }
        return true;
    }
}
