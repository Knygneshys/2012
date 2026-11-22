package com.example.server.model;

import com.example.server.model.blocks.ServerBlock;

import java.util.Objects;

public final class ServerMapTiles extends GameAggregate<ServerBlock> {
    private final ServerBlock[][] map;

    public ServerMapTiles(ServerBlock[][] map) {
        this.map = Objects.requireNonNull(map);
    }

    @Override
    public GameIterator<ServerBlock> createIterator() {
        return new MapTileIterator(map);
    }
}
