package com.example.server.interpreter;

import com.example.server.GameLogger;

/**
 * Terminal expression: prints NPC summary from the server.
 * Grammar: "npcs"
 */
public class NpcsExpression implements ConsoleExpression {

    private static final GameLogger log = GameLogger.getInstance();

    @Override
    public void interpret(ConsoleContext ctx) {
        String summary = ctx.server.getNpcSummary();
        log.info("[NPCs]\n" + summary);
    }
}
