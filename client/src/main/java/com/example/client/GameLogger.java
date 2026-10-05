package com.example.client;

import java.io.PrintStream;

/**
 * The single point of access for the client's log output.
 * <p>
 * A Singleton in the GoF sense: one instance per JVM, reachable through
 * {@link #getInstance()}, with the constructor hidden so nothing else can build
 * a second one. The client keeps its own copy for the same reason the protocol
 * message classes are duplicated rather than shared: the two modules depend on
 * nothing of each other. They are separate processes, so each having its own
 * instance is exactly what "one per JVM" means.
 * <p>
 * It qualifies because it is a stateless facade over a resource rather than a
 * bag of game state. Two things make it worth having:
 * <ul>
 *   <li><b>One place to change the format.</b> The {@code [Client]} prefix used
 *       to be retyped at every call site; a timestamp, a level filter or a file
 *       sink is now a change to this class alone.</li>
 *   <li><b>One output stream.</b> Everything the client says goes through the
 *       same handle instead of each call site opening its own.</li>
 * </ul>
 * It also has to be thread safe: the WebSocket listener, the event dispatch
 * thread that paints the map and the connect thread all log, and the JVM
 * serialises class initialisation, so the instance is fully constructed before
 * any of them can reach it.
 */
public final class GameLogger {

    /**
     * Created when this class is first touched. The JVM guarantees this
     * initialisation runs exactly once and is visible to every thread
     * afterwards, which is what makes this variant safe without a lock.
     */
    private static final GameLogger INSTANCE = new GameLogger();

    private final String prefix;

    private GameLogger() {
        this.prefix = "[Client] ";
    }

    /**
     * The one and only logger.
     */
    public static GameLogger getInstance() {
        return INSTANCE;
    }

    /**
     * Records an ordinary event, such as a connection opening or a close frame.
     */
    public void info(String message) {
        write(System.out, message, null);
    }

    /**
     * Records a failure, together with its cause when there is one.
     */
    public void error(String message, Throwable cause) {
        write(System.err, message, cause);
    }

    /**
     * Writes one whole entry at a time. Without this, two threads logging at
     * once could interleave halfway through a line and make the output
     * unreadable.
     */
    private synchronized void write(PrintStream out, String message, Throwable cause) {
        out.println(prefix + message);
        if (cause != null) {
            cause.printStackTrace(out);
        }
    }
}
