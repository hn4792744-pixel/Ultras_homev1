package com.ultrascore.messagesystem;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.settings.SettingKey;
import com.ultrascore.settings.SettingsManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Players who ran /chat to disable chat simply don't receive normal player chat
 * messages — administrative broadcasts (/bc) are a separate delivery path
 * (AnnouncementManager) and are unaffected by this toggle, per spec.
 */
public class ChatListener implements Listener {

    private final SettingsManager settings;

    public ChatListener(UltrasCore plugin, SettingsManager settings) {
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncChatEvent event) {
        event.viewers().removeIf(viewer -> {
            if (!(viewer instanceof Player player)) return false;
            if (player.equals(event.getPlayer())) return false; // always let you see your own message
            return !settings.get(player.getUniqueId(), SettingKey.CHAT);
        });
    }
}
