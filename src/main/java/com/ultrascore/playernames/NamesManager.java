package com.ultrascore.playernames;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.settings.SettingKey;
import com.ultrascore.settings.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real nametag hiding (not just a stored preference): each viewer who disables
 * Names gets their own personal Scoreboard with a team set to NameTagVisibility.NEVER
 * containing every other online player, so only THEIR client stops rendering
 * nametags — nobody else's view changes.
 */
public class NamesManager {

    private static final String TEAM_NAME = "uc_hide_names";

    private final UltrasCore plugin;
    private final SettingsManager settings;
    private final Map<UUID, Scoreboard> boards = new ConcurrentHashMap<>();

    public NamesManager(UltrasCore plugin, SettingsManager settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    private Scoreboard boardFor(Player viewer) {
        return boards.computeIfAbsent(viewer.getUniqueId(), k -> {
            Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
            viewer.setScoreboard(sb);
            return sb;
        });
    }

    public boolean namesShown(Player viewer) {
        return settings.get(viewer.getUniqueId(), SettingKey.NAMES);
    }

    public boolean toggleNames(Player viewer) {
        boolean next = settings.toggle(viewer.getUniqueId(), SettingKey.NAMES);
        applyFor(viewer);
        return next;
    }

    /** Rebuilds {@code viewer}'s hidden-nametag team from their current setting. */
    public void applyFor(Player viewer) {
        Scoreboard sb = boardFor(viewer);
        Team team = sb.getTeam(TEAM_NAME);
        boolean hide = !namesShown(viewer);

        if (!hide) {
            if (team != null) team.unregister();
            return;
        }
        if (team == null) {
            team = sb.registerNewTeam(TEAM_NAME);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(viewer) && !team.hasEntry(other.getName())) {
                team.addEntry(other.getName());
            }
        }
    }

    /** Adds the newcomer's name into every existing viewer's hidden team, if that viewer has Names off. */
    public void registerNewcomerWithExistingViewers(Player newcomer) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(newcomer) || namesShown(viewer)) continue;
            Team team = boardFor(viewer).getTeam(TEAM_NAME);
            if (team != null && !team.hasEntry(newcomer.getName())) {
                team.addEntry(newcomer.getName());
            }
        }
    }
}
