package com.example.server.model.factories;

import com.example.server.model.blocks.ServerBlock;
import com.example.server.model.blocks.ServerBreakableWall;

public final class BreakableWallFactory extends ServerBlockFactory {
    @Override
    protected ServerBlock createBlock(int x, int y) {
        return new ServerBreakableWall(x, y);
    }
}
