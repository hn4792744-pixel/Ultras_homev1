package com.ultrascore.utils;

import com.ultrascore.core.UltrasCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

/**
 * Central place for turning a MiniMessage string (from lang files or config)
 * into an Adventure Component and sending it. Also supports legacy '&' codes
 * transparently by translating them to MiniMessage-compatible tags first.
 */
public final class MessageUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private MessageUtil() {}

    public static Component render(String raw) {
        if (raw == null) return Component.empty();
        return MM.deserialize(translateLegacy(raw));
    }

    /** Sends a raw MiniMessage string with the server prefix prepended. */
    public static void sendPrefixed(UltrasCore plugin, CommandSender target, String raw) {
        String prefix = plugin.getConfigManager().getServerPrefix();
        target.sendMessage(render(prefix + " " + raw));
    }

    /** Sends a raw MiniMessage string with no prefix. */
    public static void send(CommandSender target, String raw) {
        target.sendMessage(render(raw));
    }

    // Minimal &-code -> MiniMessage bridge so config authors can keep using '&' if they prefer.
    private static String translateLegacy(String input) {
        if (input.indexOf('&') < 0) return input;
        return input
                .replace("&0", "<black>").replace("&1", "<dark_blue>").replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>").replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                .replace("&6", "<gold>").replace("&7", "<gray>").replace("&8", "<dark_gray>")
                .replace("&9", "<blue>").replace("&a", "<green>").replace("&b", "<aqua>")
                .replace("&c", "<red>").replace("&d", "<light_purple>").replace("&e", "<yellow>")
                .replace("&f", "<white>").replace("&l", "<bold>").replace("&n", "<underlined>")
                .replace("&o", "<italic>").replace("&m", "<strikethrough>").replace("&k", "<obfuscated>")
                .replace("&r", "<reset>");
    }
}
