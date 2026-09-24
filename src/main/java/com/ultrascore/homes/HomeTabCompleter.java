package com.ultrascore.homes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeTabCompleter implements TabCompleter {

    private final HomeManager manager;

    public HomeTabCompleter(HomeManager manager) {
        this.manager = manager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1 || !(sender instanceof Player player)) return List.of();

        String cmd = command.getName().toLowerCase();
        String partial = args[0].toLowerCase();

        List<String> options;
        if (cmd.equals("home_admin")) {
            options = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        } else {
            options = new ArrayList<>(manager.getHomes(player.getUniqueId()).keySet());
        }
        return options.stream().filter(o -> o.toLowerCase().startsWith(partial)).collect(Collectors.toList());
    }
}
