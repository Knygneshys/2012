package com.example.client;

public class Decoration implements IPrototype {
    private int x;
    private int y;
    private DecorationStyle style;

    public Decoration(int x, int y, DecorationStyle style) {
        this.x = x;
        this.y = y;
        this.style = style;
    }

    @Override
    public Decoration clone(boolean shallow) {
        if (shallow) {
            return new Decoration(x, y, style);
        } else {
            DecorationStyle styleCopy = new DecorationStyle(
                    style.getName(),
                    style.getColor()
            );

            return new Decoration(x, y, styleCopy);
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public DecorationStyle getStyle() {
        return style;
    }
}
