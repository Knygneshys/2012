package com.example.client;

import java.awt.Rectangle;

/**
 * Root of the game object hierarchy. Everything that occupies space in the
 * world is a GameObject: characters, bombs, powerups and map blocks.
 */
public abstract class GameObject {
    /**
     * The position of the game object on the map in
     * (x, y) coordinates, not tiles.
     */
    public record Position(int x, int y) {
    }

    public int width;
    public int height;
    Position position = new Position(0, 0);

    public GameObject(int x, int y, int width, int height) {
        this.position = new Position(x, y);
        this.width = width;
        this.height = height;
    }

    public Position getPosition() {
        return position;
    }

    public int getX() {
        return position.x();
    }

    public int getY() {
        return position.y();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setPosition(int x, int y) {
        this.position = new Position(x, y);
    }

    /**
     * The tile column this object's top left corner sits in.
     */
    public int tileX(int tileSize) {
        return position.x() / tileSize;
    }

    /**
     * The tile row this object's top left corner sits in.
     */
    public int tileY(int tileSize) {
        return position.y() / tileSize;
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x(), position.y(), width, height);
    }

    /**
     * True when this object shares at least one pixel with another.
     */
    public boolean overlaps(GameObject other) {
        return getBounds().intersects(other.getBounds());
    }

    /**
     * True when the given pixel lies inside this object.
     */
    public boolean contains(int px, int py) {
        return getBounds().contains(px, py);
    }

    /**
     * True when this object's body covers any part of the given tile. A body is
     * not always aligned to the tile grid, so it can straddle two tiles and has
     * to be caught by whatever happens on either of them.
     */
    public boolean intersectsTile(int tileX, int tileY, int tileSize) {
        int tx = tileX * tileSize;
        int ty = tileY * tileSize;
        return position.x() < tx + tileSize && position.x() + width > tx
            && position.y() < ty + tileSize && position.y() + height > ty;
    }
}