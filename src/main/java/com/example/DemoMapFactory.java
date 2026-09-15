package com.example;

public final class DemoMapFactory {
    public static final int MAP_WIDTH = 13;
    public static final int MAP_HEIGHT = 11;

    private DemoMapFactory() {
    }

    public static TileType[][] createDefaultMap() {
        TileType[][] map = new TileType[MAP_HEIGHT][MAP_WIDTH];

        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                map[y][x] = TileType.FLOOR;
            }
        }

        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                boolean border = y == 0 || y == MAP_HEIGHT - 1 || x == 0 || x == MAP_WIDTH - 1;
                boolean hardWallPattern = y % 2 == 0 && x % 2 == 0;
                if (border || hardWallPattern) {
                    map[y][x] = TileType.HARD_WALL;
                }
            }
        }

        for (int y = 1; y < MAP_HEIGHT - 1; y++) {
            for (int x = 1; x < MAP_WIDTH - 1; x++) {
                if (map[y][x] == TileType.FLOOR && (x + y) % 3 == 0) {
                    map[y][x] = TileType.SOFT_BLOCK;
                }
            }
        }

        clearSpawnArea(map, 1, 1);
        clearSpawnArea(map, MAP_WIDTH - 2, 1);
        clearSpawnArea(map, 1, MAP_HEIGHT - 2);
        clearSpawnArea(map, MAP_WIDTH - 2, MAP_HEIGHT - 2);
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
