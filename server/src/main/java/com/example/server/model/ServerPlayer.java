package com.example.server.model;

import com.example.server.GameConstants;

/**
 * A connected player. Position, speed and the movement rule come from
 * {@link ServerCharacter}; what is left here is the player's own stats and the
 * reset back to its spawn.
 */
public class ServerPlayer extends ServerCharacter {
    public String name;
    public String colorHex;
    /**
     * How many bombs this player may have on the map at the same time.
     */
    public int maxBombs = GameConstants.PLAYER_MAX_BOMBS;
    /**
     * How far this player's bombs spread. Both are raised by powerups.
     */
    public int bombRadius = GameConstants.BOMB_RADIUS;

    private final int spawnX;
    private final int spawnY;

    public ServerPlayer(int id, String name, int x, int y, String colorHex) {
        super(id, x, y, GameConstants.PLAYER_MOVE_SPEED);
        this.name = name;
        this.colorHex = colorHex;
        this.spawnX = x;
        this.spawnY = y;
    }

    public void reset() {
        x = spawnX;
        y = spawnY;
        alive = true;
        moveSpeed = GameConstants.PLAYER_MOVE_SPEED;
        maxBombs = GameConstants.PLAYER_MAX_BOMBS;
        bombRadius = GameConstants.BOMB_RADIUS;
    }
}