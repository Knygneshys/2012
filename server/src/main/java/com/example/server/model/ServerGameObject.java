package com.example.server.model;

/**
 * Root of everything the server simulates and broadcasts.
 * <p>
 * Its one job is the contract every part of the world shares: where it sits on
 * the tile grid. Characters derive that from their pixel position, blocks and
 * items are anchored to it directly. Nothing dispatches over this type, it
 * exists so that "which tile is this on" is answered the same way everywhere.
 * ServerExplosion is deliberately not one of these: a blast covers many tiles
 * rather than sitting on one.
 */
public abstract class ServerGameObject {

    /**
     * Tile column this object occupies.
     */
    public abstract int tileX();

    /**
     * Tile row this object occupies.
     */
    public abstract int tileY();
}