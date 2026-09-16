package com.example;

import java.awt.Color;

public abstract class Character extends GameObject {
    /**
     * The speed at which the character can move.
     */
    public int moveSpeed;
    /**
     * The color of the character.
     */
    public Color color;

    public Character(int x,int y,int moveSpeed, Color color, int width, int height) {
        super(x, y, width, height);
        this.moveSpeed = moveSpeed;
        this.color = color;
    }
}
