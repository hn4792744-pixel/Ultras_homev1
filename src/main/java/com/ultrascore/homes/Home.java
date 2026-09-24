package com.ultrascore.homes;

import org.bukkit.Location;
import org.bukkit.World;

public class Home {

    private final String name;
    private final String world;
    private final double x, y, z;
    private final float yaw, pitch;

    public Home(String name, String world, double x, double y, double z, float yaw, float pitch) {
        this.name = name;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static Home fromLocation(String name, Location loc) {
        return new Home(name, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /** Resolves to a live Location, or null if the world is no longer loaded. */
    public Location toLocation() {
        World w = org.bukkit.Bukkit.getWorld(world);
        if (w == null) return null;
        return new Location(w, x, y, z, yaw, pitch);
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }
}
