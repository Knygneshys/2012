package com.example.server.model;

import com.example.server.model.blocks.ServerBlock;

import com.example.server.GameConstants;
import com.example.server.mediator.GameEventMediator;

/**
 * Base class of every living entity the server simulates: the players and the
 * NPCs. Owns the state they share (identity, position, speed, alive) and the
 * single movement and overlap rule they both obey, so the two cannot drift
 * apart the way two independent copies would.
 *
 * A body is always {@link GameConstants#TILE_SIZE} square, which is what the
 * overlap helpers below assume.
 */
public abstract class ServerCharacter extends ServerGameObject {

    public final int id;
    public int x, y;
    public int moveSpeed;
    public boolean alive = true;

    private GameEventMediator mediator;

    protected ServerCharacter(int id, int x, int y, int moveSpeed) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.moveSpeed = moveSpeed;
    }

    public void setMediator(GameEventMediator mediator) {
        this.mediator = mediator;
    }

    public GameEventMediator getMediator() {
        return mediator;
    }

    /**
     * Moves the body by (dx, dy), but only when it stays fully inside the map
     * and every tile it would then cover is a passable {@link ServerBlock}. A
     * dead body never moves. Returns true when it actually moved.
     */
    public boolean move(int dx, int dy, ServerBlock[][] map) {
        if (!alive) return false;
        if (dx == 0 && dy == 0) return false;
        if (map == null || map.length == 0 || map[0].length == 0) return false;

        int newX = x + dx;
        int newY = y + dy;
        if (!canOccupy(newX, newY, map)) {
            return false;
        }

        x = newX;
        y = newY;
        return true;
    }

    /**
     * True when a body at (px, py) would be fully inside the map with every
     * tile it covers passable.
     */
    protected static boolean canOccupy(int px, int py, ServerBlock[][] map) {
        int size = GameConstants.TILE_SIZE;
        if (px < 0 || py < 0
            || px + size > map[0].length * size
            || py + size > map.length * size) {
            return false;
        }
        // Collision check with the four corners of the body.
        return isPassableAt(map, px, py)
            && isPassableAt(map, px + size - 1, py)
            && isPassableAt(map, px, py + size - 1)
            && isPassableAt(map, px + size - 1, py + size - 1);
    }

    /**
     * Kills the body. Movement is refused from here on.
     */
    public void kill() {
        this.alive = false;
    }

    @Override
    public int tileX() {
        return x / GameConstants.TILE_SIZE;
    }

    @Override
    public int tileY() {
        return y / GameConstants.TILE_SIZE;
    }

    /**
     * True when the body covers any part of the given tile. Bodies do not stay
     * aligned to the tile grid, so one that straddles two tiles has to be
     * caught by a blast hitting either of them.
     */
    public boolean overlapsTile(int tileX, int tileY) {
        int size = GameConstants.TILE_SIZE;
        int tilePixelX = tileX * size;
        int tilePixelY = tileY * size;
        return x < tilePixelX + size && x + size > tilePixelX
            && y < tilePixelY + size && y + size > tilePixelY;
    }

    /**
     * True when this body and another share at least one pixel.
     */
    public boolean overlaps(ServerCharacter other) {
        int size = GameConstants.TILE_SIZE;
        return x < other.x + size && x + size > other.x
            && y < other.y + size && y + size > other.y;
    }

    protected static boolean isPassableAt(ServerBlock[][] map, int px, int py) {
        int tileX = px / GameConstants.TILE_SIZE;
        int tileY = py / GameConstants.TILE_SIZE;
        if (tileX < 0 || tileY < 0 || tileY >= map.length || tileX >= map[0].length) {
            return false;
        }
        return map[tileY][tileX].isPassable();
    }
}