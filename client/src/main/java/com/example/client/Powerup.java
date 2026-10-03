package com.example.client;

import java.awt.Color;

/**
 * A collectable item. Powerups drop where a {@link BreakableWall} was blown up
 * and permanently improve the character that walks over them.
 */
public class Powerup extends GameObject {
    /**
     * The bonuses a powerup grants. The ordinal of each constant is what the
     * server sends over the network, so the order must not change.
     */
    public enum Kind {
        EXTRA_BOMB("Bomb", new Color(50, 50, 50)),
        BIGGER_BOMB("Range", new Color(190, 70, 70)),
        FASTER("Speed", new Color(70, 190, 90));

        public static final int EXTRA_BOMB_STEP = 1;
        public static final int BIGGER_BOMB_STEP = 1;
        public static final int FASTER_STEP = 2;

        private final String label;
        private final Color color;

        Kind(String label, Color color) {
            this.label = label;
            this.color = color;
        }

        public String label() {
            return label;
        }

        public Color color() {
            return color;
        }

        public static Kind fromOrdinal(int ordinal) {
            Kind[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                throw new IllegalArgumentException("Unknown powerup kind: " + ordinal);
            }
            return values[ordinal];
        }
    }

    public final Kind kind;
    public final int tileX;
    public final int tileY;

    public Powerup(Kind kind, int tileX, int tileY, int tileSize) {
        super(tileX * tileSize, tileY * tileSize, tileSize, tileSize);
        this.kind = kind;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    /**
     * Applies this powerup's bonus to a player.
     */
    public void applyTo(Player player) {
        switch (kind) {
            case EXTRA_BOMB -> player.maxBombs += Kind.EXTRA_BOMB_STEP;
            case BIGGER_BOMB -> player.bombRadius += Kind.BIGGER_BOMB_STEP;
            case FASTER -> player.moveSpeed += Kind.FASTER_STEP;
        }
    }

    /**
     * Stable identity of this pickup, used to apply it only once.
     */
    public String key() {
        return kind.name() + ":" + tileX + "," + tileY;
    }
}