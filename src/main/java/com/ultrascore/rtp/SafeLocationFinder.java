package com.ultrascore.rtp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.EnumSet;
import java.util.Set;

/**
 * Pure block-safety checks: no lava/water/fire/cactus/powder snow underfoot,
 * no suffocation, no obviously dangerous block right above. Kept free of any
 * scheduling concerns so it's trivially unit-testable and reusable (Warps/RTP).
 */
public final class SafeLocationFinder {

    private static final Set<Material> UNSAFE_GROUND = EnumSet.of(
            Material.LAVA, Material.WATER, Material.FIRE, Material.SOUL_FIRE,
            Material.CACTUS, Material.POWDER_SNOW, Material.MAGMA_BLOCK,
            Material.CAMPFIRE, Material.SOUL_CAMPFIRE
    );

    private SafeLocationFinder() {}

    /** True if the location is safe to teleport a player to (checks ground + a 2-block-tall body space). */
    public static boolean isSafe(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;

        Block ground = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        Block feet = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        Block head = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
        Block above = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() + 2, loc.getBlockZ());

        if (!ground.getType().isSolid()) return false;
        if (UNSAFE_GROUND.contains(ground.getType())) return false;

        if (!isPassable(feet) || !isPassable(head)) return false;
        if (UNSAFE_GROUND.contains(above.getType())) return false;

        return true;
    }

    private static boolean isPassable(Block block) {
        Material type = block.getType();
        if (type.isSolid()) return false;
        if (UNSAFE_GROUND.contains(type)) return false;
        if (type == Material.WATER || type == Material.LAVA) return false;
        return true;
    }
}
