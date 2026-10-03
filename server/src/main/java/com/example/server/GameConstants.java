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

    // Player base stats, powerups raise them during a round.
    public static final int PLAYER_MOVE_SPEED = 10;
    public static final int PLAYER_MAX_BOMBS = 1;
    public static final int BOMB_FUSE_MS = 2000;
    public static final int BOMB_RADIUS = 2;

// NPCs. The game loop ticks every TICK_MS, so the move speed is in pixels per
// tick: 1 keeps an NPC at a pace a player can still escape from.
public static final int NPC_COUNT = 3;
public static final int NPC_MOVE_SPEED = 1;
public static final String[] NPC_COLORS = {"#ff8800", "#aa00ff", "#00cccc"};
public static final double NPC_TURN_CHANCE = 0.25;
public static final int NPC_TURN_MIN_MS = 400;
public static final int NPC_TURN_MAX_MS = 1200;

    // Powerups
    public static final double POWERUP_DROP_CHANCE = 0.4;
    public static final int MAX_POWERUPS = 5;

// NPC spawning
public static final int NPC_SPAWN_ATTEMPTS = 50;
    public static final int NPC_SPAWN_CLEARANCE = 1;
}