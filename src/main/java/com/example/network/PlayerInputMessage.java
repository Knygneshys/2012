package com.example.network;

/**
 * Message sent by client to indicate player input (movement or bomb).
 */
public class PlayerInputMessage extends GameMessage {
    public int playerId;
    public String action; // "UP", "DOWN", "LEFT", "RIGHT", "BOMB", "RESET"

    public PlayerInputMessage() {
        super("PLAYER_INPUT");
    }

    public PlayerInputMessage(int playerId, String action) {
        super("PLAYER_INPUT");
        this.playerId = playerId;
        this.action = action;
    }
}

