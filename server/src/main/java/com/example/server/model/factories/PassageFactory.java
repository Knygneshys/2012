package com.example.server.model.factories;

import com.example.server.model.blocks.ServerBlock;
import com.example.server.model.blocks.ServerPassage;

public final class PassageFactory extends ServerBlockFactory {
    @Override
    protected ServerBlock createBlock(int x, int y) {
        return new ServerPassage(x, y);
    }
}
