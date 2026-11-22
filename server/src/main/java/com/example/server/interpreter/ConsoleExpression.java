package com.example.server.interpreter;

/**
 * Base interface for the Interpreter pattern.
 * Each concrete expression represents one valid console command (or grammar rule).
 */
public interface ConsoleExpression {
    /**
     * Interprets the command using the context.
     * @param ctx parsed tokens + server reference
     */
    void interpret(ConsoleContext ctx);
}
