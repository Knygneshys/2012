package com.example.client;

import com.example.client.factories.MapElementFactory;
import java.awt.Color;
import java.awt.Graphics;
import java.util.EnumMap;
import java.util.Map;

/** Shares each theme's tile appearance while the map supplies tile coordinates. */
final class TileAppearanceFactory {
    private final MapElementFactory mapElementFactory;
    private final Map<TileType, TileAppearance> appearances = new EnumMap<>(TileType.class);

    TileAppearanceFactory(MapElementFactory mapElementFactory) {
        this.mapElementFactory = mapElementFactory;
    }

    TileAppearance get(TileType type) {
        return appearances.computeIfAbsent(type,
                tile -> new TileAppearance(Block.create(0, 0, tile, 1, mapElementFactory).color()));
    }

    record TileAppearance(Color color) {
        void draw(Graphics g, int tileX, int tileY, int tileSize) {
            g.setColor(color);
            g.fillRect(tileX * tileSize, tileY * tileSize, tileSize, tileSize);
        }
    }
}
