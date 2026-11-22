package com.example.server.interpreter;

import com.example.server.GameLogger;

/**
 * Terminal expression: resets the game to its initial state.
 * Grammar: "reset"
 */
public class ResetExpression implements ConsoleExpression {

    private static final GameLogger log = GameLogger.getInstance();

    @Override
    public void interpret(ConsoleContext ctx) {
        ctx.server.resetGame();
        log.info("[Console] Game reset via console command.");
    }
}
