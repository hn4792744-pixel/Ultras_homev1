package com.ultrascore.tpa;

import com.ultrascore.core.UltrasCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TPATabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("accept", "deny", "cancel", "toggle", "gui", "all");

    private final UltrasCore plugin;

    public TPATabCompleter(UltrasCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled(command.getName().toLowerCase())) {
            return List.of();
        }
        if (args.length != 1) return List.of();

        List<String> options = new ArrayList<>(SUBCOMMANDS);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!(sender instanceof Player s) || !p.equals(s)) {
                options.add(p.getName());
            }
        }
        String partial = args[0].toLowerCase();
        return options.stream().filter(o -> o.toLowerCase().startsWith(partial)).collect(Collectors.toList());
    }
}
