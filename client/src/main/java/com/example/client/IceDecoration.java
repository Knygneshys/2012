package com.example.client;

public class IceDecoration extends Decoration {

    public IceDecoration(int x, int y, DecorationStyle style) {
        super(x, y, style);
    }

    @Override
    public IceDecoration clone(boolean shallow) {
        return new IceDecoration(
                getX(),
                getY(),
                copyStyle(shallow)
        );
    }
}
