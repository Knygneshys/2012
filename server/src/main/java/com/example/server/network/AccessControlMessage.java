package com.example.server.network;

/**
 * Sent by {@link com.example.server.proxy.GameServerProxy}: either announces
 * who the host is, or tells one client that an action it sent was refused.
 */
public class AccessControlMessage extends GameMessage {
    /** Current host's player id, or -1 when nobody is connected. */
    public int hostPlayerId;
    /** True when this message reports a refused action. */
    public boolean denied;
    /** The refused action (e.g. "RESET"), or null for a host update. */
    public String action;
    /** Human readable explanation shown to the player. */
    public String message;

    public AccessControlMessage() {
        super("ACCESS_CONTROL");
    }

    public static AccessControlMessage hostUpdate(int hostPlayerId, String message) {
        AccessControlMessage m = new AccessControlMessage();
        m.hostPlayerId = hostPlayerId;
        m.denied = false;
        m.message = message;
        return m;
    }

    public static AccessControlMessage denied(int hostPlayerId, String action, String message) {
        AccessControlMessage m = new AccessControlMessage();
        m.hostPlayerId = hostPlayerId;
        m.denied = true;
        m.action = action;
        m.message = message;
        return m;
    }
}
