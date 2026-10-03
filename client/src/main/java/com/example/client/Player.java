package com.example.client;

import java.awt.Color;

public class Player extends Character {
    public static final int DEFAULT_MAX_BOMBS = 1;
    public static final int DEFAULT_BOMB_RADIUS = 2;

    public String name;
    /**
     * Id the server assigned to this player, or -1 while offline.
     */
    public int id = -1;
    public final Color initialColor;
    public final int initialMoveSpeed;

    /**
     * How many bombs this player may have on the map at the same time.
     */
    public int maxBombs = DEFAULT_MAX_BOMBS;
    /**
     * How far the bombs of this player spread. Both are raised by powerups.
     */
    public int bombRadius = DEFAULT_BOMB_RADIUS;

    public Player(int x, int y, int moveSpeed, String name, Color color, int width, int height) {
        super(x, y, moveSpeed, color, width, height);
        this.name = name;
        this.initialColor = color;
        this.initialMoveSpeed = moveSpeed;
    }

    /**
     * Restores the state a player starts a round with, undoing powerup bonuses.
     */
    public void reset(int x, int y) {
        setPosition(x, y);
        alive = true;
        color = initialColor;
        moveSpeed = initialMoveSpeed;
        maxBombs = DEFAULT_MAX_BOMBS;
        bombRadius = DEFAULT_BOMB_RADIUS;
    }
}