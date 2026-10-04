package com.example.server.model.blocks;

import com.example.server.model.ServerGameObject;
import com.example.server.model.TileType;

/**
 * A single tile of the authoritative playfield.
 * <p>
 * Every map cell is a ServerBlock, so the geometry of the map lives in the same
 * hierarchy as the characters and items that interact with it. Subclasses only
 * differ in how they behave: a {@link ServerWall} cannot be entered and stops a
 * blast, a {@link ServerBreakableWall} cannot be entered but a blast turns it
 * into a {@link ServerPassage}, and a {@link ServerPassage} is empty walkable
 * space.
 * <p>
 * This mirrors the client Block hierarchy. The two modules share no classes, so
 * the duplication is deliberate.
 */
public abstract class ServerBlock extends ServerGameObject {

    protected final int x;
    protected final int y;
    protected final TileType tileType;

    protected ServerBlock(int x, int y, TileType tileType) {
        this.x = x;
        this.y = y;
        this.tileType = tileType;
    }

    @Override
    public int tileX() {
        return x;
    }

    @Override
    public int tileY() {
        return y;
    }

    /**
     * True when characters may occupy this tile.
     */
    public abstract boolean isPassable();

    /**
     * True when a bomb blast cannot spread past this tile.
     */
    public abstract boolean stopsBlast();

    /**
     * True when a bomb blast turns this tile into something else.
     */
    public abstract boolean isDestructible();

    /**
     * What this tile becomes once a blast destroys it. Tiles that survive
     * return themselves, which makes the identity check
     * {@code before != after} mean "this tile was destroyed".
     */
    public ServerBlock destroyed() {
        return this;
    }

    /**
     * The tile type this block is sent as. The ordinal is the wire format, so
     * the order of TileType must not change.
     */
    public TileType tileType() {
        return tileType;
    }

}
