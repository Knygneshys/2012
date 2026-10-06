package com.example.client;

import java.awt.Color;
import com.example.client.factories.MapElementFactory;

/**
 * A single tile of the playfield.
 * <p>
 * Every map cell is a Block, so the static map geometry lives in the same
 * hierarchy as the characters and items that interact with it. Subclasses only
 * differ in how they behave: a {@link Wall} cannot be entered or destroyed, a
 * {@link BreakableWall} cannot be entered but a bomb blast turns it into a
 * {@link Passage}, and a {@link Passage} is empty walkable space.
 */
public abstract class Block extends GameObject {
    protected final int tileX;
    protected final int tileY;
    protected final int tileSize;
    protected final TileType tileType;

    protected Block(int tileX, int tileY, TileType tileType, int tileSize) {
        super(tileX * tileSize, tileY * tileSize, tileSize, tileSize);
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileType = tileType;
        this.tileSize = tileSize;
    }

    /**
     * True when characters and items may occupy this tile.
     */
    public abstract boolean isPassable();

    /**
     * True when a bomb blast can destroy this block.
     */
    public abstract boolean isDestructible();

    /**
     * Fill colour used when rendering this block.
     */
    public abstract Color color();

    /**
     * What this block becomes after a bomb blast hits it. Blocks that survive
     * return themselves, which makes the identity check
     * {@code before != after} mean "this block was destroyed".
     */
    public Block destroyed() {
        return this;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public int getTileSize() {
        return tileSize;
    }

    public TileType getTileType() {
        return tileType;
    }

    /**
     * Builds the block that represents the given tile type.
     */
    public static Block create(int tileX, int tileY, TileType tileType, int tileSize, MapElementFactory factory) {
        return switch (tileType) {
            case HARD_WALL -> factory.createWall(tileX, tileY, tileSize);
            case SOFT_BLOCK -> factory.createBreakableWall(tileX, tileY, tileSize);
            case FLOOR -> factory.createPassage(tileX, tileY, tileSize);
        };
    }

    /**
     * Converts a tile map into the block objects used by rendering and collision.
     */
    public static Block[][] fromMap(TileType[][] map, int tileSize, MapElementFactory factory) {
        Block[][] blocks = new Block[map.length][map[0].length];
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                blocks[y][x] = create(x, y, map[y][x], tileSize, factory);
            }
        }

        return blocks;
    }
}