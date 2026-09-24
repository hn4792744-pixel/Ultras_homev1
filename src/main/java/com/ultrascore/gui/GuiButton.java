package com.ultrascore.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/** A clickable item: what it looks like, and what happens when a player clicks it. */
public class GuiButton {

    private final ItemStack icon;
    private final Consumer<Player> onClick;

    public GuiButton(ItemStack icon, Consumer<Player> onClick) {
        this.icon = icon;
        this.onClick = onClick;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void click(Player player) {
        if (onClick != null) {
            onClick.accept(player);
        }
    }
}
