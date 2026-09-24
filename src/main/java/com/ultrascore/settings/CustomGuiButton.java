package com.ultrascore.settings;

public class CustomGuiButton {

    public int slot;
    public String material = "STONE";
    public String name = "";
    public java.util.List<String> lore = java.util.List.of();
    public String permission; // null = no permission required
    public boolean enabled = true;
    public String command;    // run as the clicking player, without leading '/'
    public String action;     // built-in keyword, e.g. "open_settings"
    public String targetGui;  // id of another settings/*.yml GUI to open
    public String sound;      // Bukkit Sound name, optional
}
