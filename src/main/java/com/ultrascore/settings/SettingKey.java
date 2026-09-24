package com.ultrascore.settings;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central catalogue of every per-player ON/OFF toggle shown in the Settings GUI.
 * Adding a new toggle to the game only requires adding one entry here — the GUI,
 * storage and command layer are all generic over this list.
 */
public final class SettingKey {

    private static final Map<String, SettingKey> REGISTRY = new LinkedHashMap<>();

    public static final SettingKey CHAT = of("chat", "Chat Messages", true);
    public static final SettingKey PRIVATE_MESSAGES = of("pm", "Private Messages", true);
    public static final SettingKey JOIN_MESSAGES = of("join-messages", "Join Messages", true);
    public static final SettingKey LEAVE_MESSAGES = of("leave-messages", "Leave Messages", true);
    public static final SettingKey DEATH_MESSAGES = of("death-messages", "Death Messages", true);
    public static final SettingKey HOME_SOUNDS = of("home-sounds", "Home Sounds", true);
    public static final SettingKey HOME_ANIMATION = of("home-animation", "Home Animation", true);
    public static final SettingKey RTP_SOUNDS = of("rtp-sounds", "RTP Sounds", true);
    public static final SettingKey RTP_ANIMATION = of("rtp-animation", "RTP Animation", true);
    public static final SettingKey SCOREBOARD = of("scoreboard", "Scoreboard", true);
    public static final SettingKey TAB = of("tab", "TAB List Extras", true);
    public static final SettingKey HIDE_PLAYERS = of("hide-players", "Hide Other Players", false);
    public static final SettingKey NAMES = of("names", "Player Names", true);
    public static final SettingKey RANKS = of("ranks", "Player Ranks", true);
    public static final SettingKey NIGHT_VISION = of("nightvision", "Night Vision", false);
    public static final SettingKey ANNOUNCEMENTS = of("announcements", "Announcements", true);

    private final String key;
    private final String label;
    private final boolean defaultValue;

    private SettingKey(String key, String label, boolean defaultValue) {
        this.key = key;
        this.label = label;
        this.defaultValue = defaultValue;
    }

    private static SettingKey of(String key, String label, boolean defaultValue) {
        SettingKey s = new SettingKey(key, label, defaultValue);
        REGISTRY.put(key, s);
        return s;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public boolean getDefault() { return defaultValue; }

    public static Map<String, SettingKey> all() {
        return REGISTRY;
    }
}
