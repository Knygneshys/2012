package com.example.client;

import java.awt.Color;

public class DecorationStyle {
    private String name;
    private Color color;

    public DecorationStyle(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
