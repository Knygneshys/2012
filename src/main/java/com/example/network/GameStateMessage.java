package com.example.network;

/**
 * Message sent from server to all clients with the current game state.
 */
public class GameStateMessage extends GameMessage {
    public GameStateData gameState;

    public GameStateMessage() {
        super("GAME_STATE");
    }

    public GameStateMessage(GameStateData gameState) {
        super("GAME_STATE");
        this.gameState = gameState;
    }
}

