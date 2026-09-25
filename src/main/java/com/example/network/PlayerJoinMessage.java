package com.example.network;

/**
 * Message sent when a player joins the game.
 */
public class PlayerJoinMessage extends GameMessage {
    public String playerName;
    public int playerId;

    public PlayerJoinMessage() {
        super("PLAYER_JOIN");
    }

    public PlayerJoinMessage(String playerName, int playerId) {
        super("PLAYER_JOIN");
        this.playerName = playerName;
        this.playerId = playerId;
    }
}

