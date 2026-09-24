package com.ultrascore.warps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Warp {

    public enum Type { PUBLIC, PRIVATE, PASSWORD }

    private final String name;
    private String world;
    private double x, y, z;
    private float yaw, pitch;
    private Type type;
    private String passwordHash; // null unless type == PASSWORD
    private final Set<UUID> members = new LinkedHashSet<>();
    private UUID owner;

    public Warp(String name) {
        this.name = name;
        this.type = Type.PUBLIC;
    }

    public String getName() { return name; }

    public void setLocation(Location loc) {
        this.world = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    public Location toLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) return null;
        return new Location(w, x, y, z, yaw, pitch);
    }

    public String getWorld() { return world; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String hash) { this.passwordHash = hash; }

    public Set<UUID> getMembers() { return members; }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    public boolean canAccess(UUID uuid, boolean bypass) {
        if (bypass) return true;
        if (type == Type.PUBLIC) return true;
        if (uuid.equals(owner)) return true;
        return members.contains(uuid);
    }
}
