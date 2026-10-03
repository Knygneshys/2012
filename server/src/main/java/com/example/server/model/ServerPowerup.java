package com.example.server.model;

import java.util.Random;

/**
 * Server side counterpart of the client Powerup. Drops on the tile where a
 * breakable wall was destroyed and is consumed by the first player that walks
 * over it.
 */
public class ServerPowerup {
    /**
     * The bonuses a powerup grants. The ordinal of each constant is what gets
     * sent over the network, so the order must match the client Powerup.Kind.
     */
    public enum Kind {
        EXTRA_BOMB,
        BIGGER_BOMB,
        FASTER;

        public static final int EXTRA_BOMB_STEP = 1;
        public static final int BIGGER_BOMB_STEP = 1;
        public static final int FASTER_STEP = 2;

        public static Kind random(Random random) {
            Kind[] values = values();
            return values[random.nextInt(values.length)];
        }
    }

    public final Kind kind;
    public final int tileX;
    public final int tileY;

    public ServerPowerup(Kind kind, int tileX, int tileY) {
        this.kind = kind;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    /**
     * Applies this powerup's bonus to a player.
     */
    public void applyTo(ServerPlayer player) {
        switch (kind) {
            case EXTRA_BOMB -> player.maxBombs += Kind.EXTRA_BOMB_STEP;
            case BIGGER_BOMB -> player.bombRadius += Kind.BIGGER_BOMB_STEP;
            case FASTER -> player.moveSpeed += Kind.FASTER_STEP;
        }
    }
}