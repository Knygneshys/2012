package com.example.server;

/** Whether players are waiting in the lobby or a round is being played. */
public enum GamePhase {
    /** Players can join; the world is frozen until the host starts the game. */
    LOBBY,
    /** A round is running. */
    PLAYING
}
