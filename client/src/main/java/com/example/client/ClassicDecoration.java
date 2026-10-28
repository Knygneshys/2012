package com.example.client;

public class ClassicDecoration extends Decoration {

    public ClassicDecoration(int x, int y, DecorationStyle style) {
        super(x, y, style);
    }

    @Override
    public ClassicDecoration clone(boolean shallow) {
        return new ClassicDecoration(
                getX(),
                getY(),
                copyStyle(shallow)
        );
    }
}
