package com.ultrascore.nightvision;

import com.ultrascore.core.UltrasCore;
import com.ultrascore.settings.SettingKey;
import com.ultrascore.settings.SettingsManager;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightVisionManager {

    private final UltrasCore plugin;
    private final SettingsManager settings;

    public NightVisionManager(UltrasCore plugin, SettingsManager settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public boolean isOn(Player player) {
        return settings.get(player.getUniqueId(), SettingKey.NIGHT_VISION);
    }

    public boolean toggle(Player player) {
        boolean next = !isOn(player);
        setOn(player, next);
        return next;
    }

    public void setOn(Player player, boolean on) {
        settings.set(player.getUniqueId(), SettingKey.NIGHT_VISION, on);
        apply(player);
    }

    /** Re-applies (or removes) the potion effect from the stored setting — used on join too. */
    public void apply(Player player) {
        if (isOn(player)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                    PotionEffect.INFINITE_DURATION, 0, true, false, false));
        } else {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }
}
