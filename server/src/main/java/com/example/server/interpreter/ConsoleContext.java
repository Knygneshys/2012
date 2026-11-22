package com.example.server.interpreter;

import com.example.server.GameServer;

/**
 * Interpreter context: carries the parsed tokens and the reference to the
 * game server so that every expression can call back into the game without
 * depending on each other.
 */
public class ConsoleContext {

    public final GameServer server;
    /** All whitespace-split tokens from the input line. tokens[0] is the command name. */
    public final String[] tokens;

    public ConsoleContext(GameServer server, String[] tokens) {
        this.server = server;
        this.tokens = tokens;
    }

    /** Returns the token at index i, or null when out of range. */
    public String arg(int i) {
        return i < tokens.length ? tokens[i] : null;
    }
}
