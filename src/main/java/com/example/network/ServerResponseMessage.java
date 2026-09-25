package com.example.network;

/**
 * General message from server to client for acknowledgments and responses.
 */
public class ServerResponseMessage extends GameMessage {
    public boolean success;
    public String message;
    public int playerId;

    public ServerResponseMessage() {
        super("SERVER_RESPONSE");
    }

    public ServerResponseMessage(boolean success, String message, int playerId) {
        super("SERVER_RESPONSE");
        this.success = success;
        this.message = message;
        this.playerId = playerId;
    }
}

