package com.example.client;

import com.example.client.command.BackToLobbyCommand;
import com.example.client.command.CommandDispatcher;
import com.example.client.command.OpenPauseMenuCommand;
import com.example.client.command.ResetGameCommand;
import com.example.client.command.StartGameCommand;
import com.example.client.network.NetworkClient;
import com.example.client.network.GameStateData;
import com.example.client.network.GameStateMessage;
import com.example.client.observer.HudStateObserver;
import com.example.client.observer.MapStateObserver;
import com.example.client.observer.NpcStateObserver;
import com.example.client.observer.PlayerStateObserver;
import com.example.client.observer.PowerupStateObserver;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Deque;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Turns key presses into network messages, and wires the game's update
 * plumbing together.
 * <p>
 * What it deliberately no longer does is interpret a server update. It used to:
 * one fifty-line method re-parsed the tile grid, rebuilt the NPC and powerup
 * lists, and split the player list into local and remote, all because it was
 * the single listener the network layer allowed. That is the fan-out that
 * {@link GameStateSubject} and the observers now own, and what is left here is
 * the part that is genuinely this class's job — deciding which observers exist,
 * and turning keys into actions.
 * <p>
 * It is the composition root for the update path, which is why it is the one
 * place that names every observer. Adding a fifth observer is a line here and
 * nothing else.
 */
public class GameController {

    private final Player player;
    private final MapPanel mapPanel;
    private final NetworkClient networkClient;
    private final GameStateSubject gameState = new GameStateSubject();
    private final CommandDispatcher commandDispatcher;
    private final HudBar hudBar;

    /**
     * How often a held direction key re-sends its move. Driving movement from
     * this timer rather than the OS key repeat removes the repeat delay (one
     * step, pause, then slow steps) that made it hard to run from a bomb.
     * Must stay above the server proxy's MIN_MOVE_INTERVAL_MS (25 ms).
     */
    private static final int MOVE_REPEAT_MS = 45;
    private static final Set<Integer> MOVE_KEYS = Set.of(
            KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D,
            KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT);
    /** Held direction keys, most recently pressed first: that one wins. */
    private final Deque<Integer> heldMoves = new ArrayDeque<>();
    private final Timer moveTimer;
    private final InGameMenu menu;
    /** Newest server state not yet shown. Older ones are dropped, never queued. */
    private final AtomicReference<GameStateData> pendingState = new AtomicReference<>();
    /** What "Leave game" does; the multiplayer app swaps this for "back to the title screen". */
    private Runnable onLeave = () -> System.exit(0);

    public GameController(Player player, MapPanel mapPanel, TileType[][] map, NetworkClient networkClient) {
        this.player = player;
        this.mapPanel = mapPanel;
        this.networkClient = networkClient;
        this.commandDispatcher = new CommandDispatcher(networkClient);
        this.hudBar = new HudBar(mapPanel.getPreferredSize().width);

        // Each observer is handed only what it needs and told about only one
        // slice of the update. The local player id is read through a supplier
        // because the server issues it after this constructor has run.
        gameState.subscribe(new MapStateObserver(mapPanel));
        gameState.subscribe(new NpcStateObserver(mapPanel));
        gameState.subscribe(new PowerupStateObserver(mapPanel));
        gameState.subscribe(new PlayerStateObserver(player, mapPanel, networkClient::getPlayerId));
        gameState.subscribe(new HudStateObserver(hudBar, mapPanel,
                networkClient::getPlayerId, networkClient::getHostPlayerId));

        networkClient.setOnGameStateUpdate(this::onGameStateUpdate);

        // Host updates and refused actions from the server's protection proxy
        mapPanel.setHostInfo(networkClient.getHostPlayerId(), networkClient.getPlayerId());
        networkClient.setOnAccessControl(msg -> SwingUtilities.invokeLater(() -> {
            mapPanel.setHostInfo(msg.hostPlayerId, networkClient.getPlayerId());
            if (msg.denied) {
                mapPanel.showNotice(msg.message);
            } else if (networkClient.isHost()) {
                mapPanel.showNotice("You are the host");
            }
        }));

        menu = new InGameMenu(networkClient, mapPanel::isLobby,
                () -> mapPanel.getTheme().label(), mapPanel::cycleTheme,
                new StartGameCommand(networkClient),
                new ResetGameCommand(networkClient),
                new BackToLobbyCommand(networkClient),
                this::leave);
        mapPanel.setMenu(menu);
        commandDispatcher.register(KeyEvent.VK_ESCAPE, new OpenPauseMenuCommand(menu));

        moveTimer = new Timer(MOVE_REPEAT_MS, e -> {
            Integer key = heldMoves.peekFirst();
            if (key != null) commandDispatcher.execute(key);
        });

        mapPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (menu.isOpen()) {           // the menu takes every key while it is open
                    menu.handleKey(key);
                    mapPanel.repaint();
                    return;
                }
                if (MOVE_KEYS.contains(key)) {
                    if (heldMoves.contains(key)) return; // OS auto-repeat: the timer handles it
                    heldMoves.addFirst(key);
                    commandDispatcher.execute(key);       // react on the very first frame
                    moveTimer.restart();
                    return;
                }
                if (key == KeyEvent.VK_ENTER && !mapPanel.isLobby()) return; // START only means something in the lobby
                commandDispatcher.execute(key);
                if (menu.isOpen()) stopMoving(); // Esc opened the menu: don't keep walking
                mapPanel.repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                heldMoves.remove(e.getKeyCode());
                if (heldMoves.isEmpty()) moveTimer.stop();
            }
        });
        // A key released while another window has focus never reaches us, so
        // forget held keys when focus goes away.
        mapPanel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                stopMoving();
            }
        });

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (menu.isOpen()) {
                    menu.handleMouseMove(e.getPoint());
                    mapPanel.repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                mapPanel.requestFocusInWindow();
                if (menu.isOpen()) {
                    menu.handleClick(e.getPoint());
                    mapPanel.repaint();
                }
            }
        };
        mapPanel.addMouseListener(mouse);
        mapPanel.addMouseMotionListener(mouse);
    }

    private void stopMoving() {
        heldMoves.clear();
        moveTimer.stop();
    }

    private void leave() {
        stopMoving();
        mapPanel.stop();
        networkClient.disconnect();
        onLeave.run();
    }

    /** Sets what happens after "Leave game" has disconnected. */
    public void setOnLeave(Runnable onLeave) {
        this.onLeave = onLeave;
    }

    /** The bar to place above the map. */
    public HudBar getHudBar() {
        return hudBar;
    }

    /**
     * Called when the server sends a game state update. It arrives on the socket
     * thread, so the subject publishes on the event dispatch thread that also
     * paints — which is what lets the observers touch Swing components directly.
     */
    private void onGameStateUpdate(GameStateMessage msg) {
        // The server broadcasts every 10 ms. Queuing every update made the
        // screen fall behind the real positions whenever painting was slower
        // than that, so the character seemed to drift on after a key was
        // released. Only the newest state is kept; a publish is scheduled only
        // if none is already waiting.
        if (pendingState.getAndSet(msg.gameState) == null) {
            SwingUtilities.invokeLater(() -> {
                GameStateData latest = pendingState.getAndSet(null);
                if (latest != null) gameState.publish(latest);
            });
        }
    }
}