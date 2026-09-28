package com.example.server;

public final class GameConstants {
    private GameConstants() {}

    public static final int DEFAULT_PORT = 8080;
    public static final int MAX_PLAYERS = 4;
    public static final int TICK_MS = 10;
    public static final int TILE_SIZE = 40;
    public static final int MAP_WIDTH = 13;
    public static final int MAP_HEIGHT = 11;

    public static final int[] SPAWN_X = {1, 11, 1, 11};
    public static final int[] SPAWN_Y = {1, 1, 9, 9};
    public static final String[] PLAYER_COLORS = {"#0000ff", "#ff0000", "#ffff00", "#00ff00"};
}
