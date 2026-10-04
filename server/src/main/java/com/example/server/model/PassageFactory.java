package com.example.server.model;

public final class PassageFactory extends ServerBlockFactory {
    @Override
    protected ServerBlock createBlock(int x, int y) {
        return new ServerPassage(x, y);
    }
}
