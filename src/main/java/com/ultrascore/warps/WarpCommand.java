package com.ultrascore.warps;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandExecutor {

    private final UltrasCore plugin;
    private final WarpManager manager;

    public WarpCommand(UltrasCore plugin, WarpManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfigManager().isCommandEnabled("warp")) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.system-disabled"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            MessageUtil.sendPrefixed(plugin, sender, plugin.getLanguageManager().get("general.player-only"));
            return true;
        }

        if (args.length == 0) {
            plugin.getGuiManager().open(player, new WarpGui(plugin, manager, player));
            return true;
        }

        String sub = args[0].toLowerCase();
        boolean isAdminSub = switch (sub) {
            case "create", "remove", "setpassword", "addpassword", "removepassword",
                    "public", "private", "set", "add", "members" -> true;
            default -> false;
        };
        if (isAdminSub && !player.hasPermission("ultrascore.warp.admin")) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.no-permission"));
            return true;
        }

        switch (sub) {
            case "list" -> listWarps(player);
            case "create" -> create(player, args);
            case "remove" -> remove(player, args);
            case "setpassword", "addpassword" -> setPassword(player, args);
            case "removepassword" -> removePassword(player, args);
            case "public" -> setType(player, args, Warp.Type.PUBLIC);
            case "private" -> setType(player, args, Warp.Type.PRIVATE);
            case "set" -> relocate(player, args);
            case "add" -> addMember(player, args);
            case "members" -> listMembers(player, args);
            default -> teleport(player, args[0]);
        }
        return true;
    }

    private void teleport(Player player, String name) {
        Warp warp = manager.get(name);
        if (warp == null) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.not-found", "warp", name));
            return;
        }
        manager.attemptTeleport(player, warp);
    }

    private void listWarps(Player player) {
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.list-header"));
        boolean bypass = player.hasPermission("ultrascore.warp.bypass");
        for (Warp warp : manager.all().values()) {
            if (!warp.canAccess(player.getUniqueId(), bypass) && warp.getType() != Warp.Type.PASSWORD) continue;
            MessageUtil.send(player, "<gray> - <white>" + warp.getName() + " <dark_gray>(" + warp.getType() + ")");
        }
    }

    private void create(Player player, String[] args) {
        if (args.length < 2) {
            usage(player, "/warp create <name>");
            return;
        }
        String name = args[1];
        if (manager.exists(name)) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.already-exists", "warp", name));
            return;
        }
        manager.create(name, player.getLocation(), player.getUniqueId());
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.created", "warp", name));
    }

    private void remove(Player player, String[] args) {
        if (args.length == 3) {
            // /warp remove <warp> <player> -> remove member
            Warp warp = manager.get(args[1]);
            if (warp == null) {
                MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.not-found", "warp", args[1]));
                return;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            warp.getMembers().remove(target.getUniqueId());
            manager.save(warp);
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager()
                    .get("warps.member-removed", "player", args[2], "warp", warp.getName()));
            return;
        }
        if (args.length < 2) {
            usage(player, "/warp remove <name>");
            return;
        }
        boolean ok = manager.remove(args[1]);
        if (ok) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.removed", "warp", args[1]));
        } else {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.not-found", "warp", args[1]));
        }
    }

    private void setPassword(Player player, String[] args) {
        if (args.length < 3) {
            usage(player, "/warp setpassword <name> <password>");
            return;
        }
        Warp warp = manager.get(args[1]);
        if (warp == null) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.not-found", "warp", args[1]));
            return;
        }
        warp.setType(Warp.Type.PASSWORD);
        warp.setPasswordHash(WarpManager.hash(args[2]));
        manager.save(warp);
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.password-set", "warp", warp.getName()));
    }

    private void removePassword(Player player, String[] args) {
        if (args.length < 2) {
            usage(player, "/warp removepassword <name>");
            return;
        }
        Warp warp = manager.get(args[1]);
        if (warp == null) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.not-found", "warp", args[1]));
            return;
        }
        warp.setPasswordHash(null);
        warp.setType(Warp.Type.PUBLIC);
        manager.save(warp);
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.password-removed", "warp", warp.getName()));
    }

    private void setType(Player player, String[] args, Warp.Type type) {
        if (args.length < 2) {
            usage(player, "/warp " + type.name().toLowerCase() + " <name>");
            return;
        }
        Warp warp = manager.get(args[1]);
        if (warp == null) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.not-found", "warp", args[1]));
            return;
        }
        warp.setType(type);
        if (type != Warp.Type.PASSWORD) warp.setPasswordHash(null);
        manager.save(warp);
        String key = type == Warp.Type.PUBLIC ? "warps.now-public" : "warps.now-private";
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get(key, "warp", warp.getName()));
    }

    private void relocate(Player player, String[] args) {
        if (args.length < 2) {
            usage(player, "/warp set <name>");
            return;
        }
        Warp warp = manager.get(args[1]);
        if (warp == null) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.not-found", "warp", args[1]));
            return;
        }
        warp.setLocation(player.getLocation());
        manager.save(warp);
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.updated", "warp", warp.getName()));
    }

    private void addMember(Player player, String[] args) {
        if (args.length < 3) {
            usage(player, "/warp add <warp> <player>");
            return;
        }
        Warp warp = manager.get(args[1]);
        if (warp == null) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.not-found", "warp", args[1]));
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
        warp.getMembers().add(target.getUniqueId());
        manager.save(warp);
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager()
                .get("warps.member-added", "player", args[2], "warp", warp.getName()));
    }

    private void listMembers(Player player, String[] args) {
        if (args.length < 2) {
            usage(player, "/warp members <name>");
            return;
        }
        Warp warp = manager.get(args[1]);
        if (warp == null) {
            MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.not-found", "warp", args[1]));
            return;
        }
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("warps.members-header", "warp", warp.getName()));
        for (var uuid : warp.getMembers()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            MessageUtil.send(player, "<gray> - <white>" + (op.getName() != null ? op.getName() : uuid));
        }
    }

    private void usage(Player player, String usage) {
        MessageUtil.sendPrefixed(plugin, player, plugin.getLanguageManager().get("general.invalid-args", "usage", usage));
    }
}
