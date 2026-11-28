package com.example.client;

public class DesertDecoration extends Decoration {
    public DesertDecoration(int x, int y, DecorationStyle style) {
        super(x, y, style);
    }

    @Override
    public DesertDecoration clone(boolean shallow) {
        return new DesertDecoration(getX(), getY(), copyStyle(shallow));
    }
}
