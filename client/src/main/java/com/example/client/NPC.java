package com.example.client;

import java.awt.Color;

/**
 * A non player character. NPCs are simulated by the server so that every
 * client sees the same one, this class is the client's view of a single NPC.
 */
public class NPC extends Character {
    public final int id;
    public final String name;

    public NPC(int id, String name, int x, int y, int moveSpeed, Color color,
               int width, int height, boolean alive) {
        super(x, y, moveSpeed, color, width, height);
        this.id = id;
        this.name = name;
        this.alive = alive;
    }

    /**
     * Applies the authoritative state the server sent for this NPC.
     */
    public void updateFromServer(int x, int y, boolean alive) {
        setPosition(x, y);
        this.alive = alive;
    }
}