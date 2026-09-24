package com.ultrascore.tpa;

import java.util.UUID;

/** A pending TPA/TPAHere request from one player to another. */
public class TPARequest {

    public enum Type { TO, HERE }

    private final UUID requester;
    private final UUID target;
    private final Type type;
    private final long createdAtMillis;

    public TPARequest(UUID requester, UUID target, Type type) {
        this.requester = requester;
        this.target = target;
        this.type = type;
        this.createdAtMillis = System.currentTimeMillis();
    }

    public UUID getRequester() {
        return requester;
    }

    public UUID getTarget() {
        return target;
    }

    public Type getType() {
        return type;
    }

    public boolean isExpired(int expireSeconds) {
        return System.currentTimeMillis() - createdAtMillis > expireSeconds * 1000L;
    }
}
