package com.example.server.model;

import com.example.server.model.blocks.ServerBlock;

import java.util.NoSuchElementException;

public final class MapTileIterator extends GameIterator<ServerBlock> {
    private final ServerBlock[][] map;
    private int row;
    private int column;

    MapTileIterator(ServerBlock[][] map) {
        this.map = map;
    }

    @Override
    public boolean hasNext() {
        while (row < map.length && column >= map[row].length) {
            row++;
            column = 0;
        }
        return row < map.length;
    }

    @Override
    public ServerBlock next() {
        if (!hasNext()) throw new NoSuchElementException();
        return map[row][column++];
    }
}
