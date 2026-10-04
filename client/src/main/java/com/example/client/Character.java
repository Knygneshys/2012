package com.example.client;

import java.awt.Color;

/**
 * Base class of every living entity in the game: the player and the NPCs.
 * Holds the shared movement rules, which are driven by {@link Block}
 * passability rather than by tile constants.
 */
public abstract class Character extends GameObject {
    /**
     * Colour a character is drawn with once a bomb blast has killed it.
     */
    public static final Color DEAD_COLOR = Color.GRAY;

    /**
     * The speed at which the character can move.
     */
    public int moveSpeed;
    /**
     * The color of the character.
     */
    public Color color;
    /**
     * False once the character has been caught in a bomb blast.
     */
    public boolean alive = true;

    public Character(int x, int y, int moveSpeed, Color color, int width, int height) {
        super(x, y, width, height);
        this.moveSpeed = moveSpeed;
        this.color = color;
    }

    /**
     * Moves the character by (dx, dy), but only when every tile its body would
     * cover is a passable {@link Block}. Returns true when it actually moved.
     */
    public boolean moveBy(int dx, int dy, Block[][] blocks) {
        if (dx == 0 && dy == 0 || blocks.length == 0 || blocks[0].length == 0) {
            return false;
        }

        int tileSize = blocks[0][0].getTileSize();
        int newX = position.x() + dx;
        int newY = position.y() + dy;

        if (newX < 0 || newY < 0
            || newX + width > blocks[0].length * tileSize
            || newY + height > blocks.length * tileSize) {
            return false;
        }

        // Collision check with the four corners of the body.
        if (!isPassableAt(blocks, tileSize, newX, newY)
            || !isPassableAt(blocks, tileSize, newX + width - 1, newY)
            || !isPassableAt(blocks, tileSize, newX, newY + height - 1)
            || !isPassableAt(blocks, tileSize, newX + width - 1, newY + height - 1)) {
            return false;
        }

        position = new Position(newX, newY);
        return true;
    }

    /**
     * Kills the character, stopping any further movement.
     */
    public void kill() {
        this.alive = false;
        this.moveSpeed = 0;
    }

    private static boolean isPassableAt(Block[][] blocks, int tileSize, int px, int py) {
        int tileX = px / tileSize;
        int tileY = py / tileSize;
        if (tileX < 0 || tileY < 0 || tileY >= blocks.length || tileX >= blocks[0].length) {
            return false;
        }
        return blocks[tileY][tileX].isPassable();
    }
}