
package com.example.client;

public abstract class Decoration implements IPrototype {
    private int x;
    private int y;
    private DecorationStyle style;

    public Decoration(int x, int y, DecorationStyle style) {
        this.x = x;
        this.y = y;
        this.style = style;
    }

    @Override
    public abstract Decoration clone(boolean shallow);

    protected DecorationStyle copyStyle(boolean shallow) {
        if (shallow) {
            return style;
        }

        return new DecorationStyle(
                style.getName(),
                style.getColor()
        );
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
