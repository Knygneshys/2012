package com.example.server.interpreter;

import com.example.server.GameLogger;

/**
 * Terminal expression: prints the number of active bombs on the map.
 * Grammar: "bombs"
 */
public class BombsExpression implements ConsoleExpression {

    private static final GameLogger log = GameLogger.getInstance();

    @Override
    public void interpret(ConsoleContext ctx) {
        int count = ctx.server.getActiveBombCount();
        log.info("[Bombs] Active bombs on map: " + count);
    }
}
