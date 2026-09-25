package com.example.network;

/**
 * Base class for all network messages between client and server.
 */
public abstract class GameMessage {
    public String messageType;

    public GameMessage(String messageType) {
        this.messageType = messageType;
    }
}

