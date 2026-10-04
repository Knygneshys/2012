package com.example.server.model;

import com.example.server.model.blocks.ServerBlock;
import com.example.server.model.factories.BreakableWallFactory;
import com.example.server.model.factories.PassageFactory;
import com.example.server.model.factories.ServerBlockFactory;
import com.example.server.model.factories.WallFactory;

import com.example.server.GameConstants;
import java.util.Map;
import java.util.Random;

public final class ServerMapFactory {
    private static final Map<TileType, ServerBlockFactory> FACTORIES = Map.of(
            TileType.HARD_WALL, new WallFactory(),
            TileType.SOFT_BLOCK, new BreakableWallFactory(),
            TileType.FLOOR, new PassageFactory()
    );

    private ServerMapFactory() {}

    public static ServerBlock[][] createDefaultMap() {
        TileType[][] layout = createLayout();
        ServerBlock[][] map = new ServerBlock[layout.length][layout[0].length];

        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[0].length; x++) {
                FACTORIES.get(layout[y][x]).placeBlock(map, x, y);
            }
        }
        return map;
    }

    /**
     * Lays out the map as plain tile types, which is how it is described before
     * it becomes blocks. The wire format sends these ordinals, so the enum order
     * is part of the protocol.
     */
    private static TileType[][] createLayout() {
        TileType[][] map = new TileType[GameConstants.MAP_HEIGHT][GameConstants.MAP_WIDTH];

        for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConstants.MAP_WIDTH; x++) {
                map[y][x] = TileType.FLOOR;
            }
        }

        for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConstants.MAP_WIDTH; x++) {
                boolean border = y == 0 || y == GameConstants.MAP_HEIGHT - 1 || x == 0 || x == GameConstants.MAP_WIDTH - 1;
                boolean hardWallPattern = y % 2 == 0 && x % 2 == 0;
                if (border || hardWallPattern) {
                    map[y][x] = TileType.HARD_WALL;
                }
            }
        }

        // Randomize soft blocks
        Random rnd = new Random();
        double softBlockProbability = 0.35;
        for (int y = 1; y < GameConstants.MAP_HEIGHT - 1; y++) {
            for (int x = 1; x < GameConstants.MAP_WIDTH - 1; x++) {
                if (map[y][x] == TileType.FLOOR && rnd.nextDouble() < softBlockProbability) {
                    map[y][x] = TileType.SOFT_BLOCK;
                }
            }
        }

        clearSpawnArea(map, 1, 1);
        clearSpawnArea(map, GameConstants.MAP_WIDTH - 2, 1);
        clearSpawnArea(map, 1, GameConstants.MAP_HEIGHT - 2);
        clearSpawnArea(map, GameConstants.MAP_WIDTH - 2, GameConstants.MAP_HEIGHT - 2);
        return map;
    }

    private static void clearSpawnArea(TileType[][] map, int x, int y) {
        map[y][x] = TileType.FLOOR;
        map[y][x - 1] = TileType.FLOOR;
        map[y][x + 1] = TileType.FLOOR;
        map[y - 1][x] = TileType.FLOOR;
        map[y + 1][x] = TileType.FLOOR;
    }
}
