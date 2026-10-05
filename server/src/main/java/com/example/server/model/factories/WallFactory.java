package com.example.server.model.factories;

import com.example.server.model.blocks.ServerBlock;
import com.example.server.model.blocks.ServerWall;

public final class WallFactory extends ServerBlockFactory {
    @Override
    protected ServerBlock createBlock(int x, int y) {
        return new ServerWall(x, y);
    }
}
