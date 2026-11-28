package com.example.client.network;

/**
 * Sent by the server's protection proxy: either announces who the host is, or
 * tells this client that an action it sent was refused.
 */
public class AccessControlMessage extends GameMessage {
    public int hostPlayerId;
    public boolean denied;
    public String action;
    public String message;

    public AccessControlMessage() {
        super("ACCESS_CONTROL");
    }
}
