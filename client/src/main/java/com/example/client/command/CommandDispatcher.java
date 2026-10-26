package com.example.client.command;

import com.example.client.network.NetworkClient;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {
    private final Map<Integer, Command> commands = new HashMap<>();

    public CommandDispatcher(NetworkClient networkClient) {
        commands.put(KeyEvent.VK_W, new MoveUpCommand(networkClient));
        commands.put(KeyEvent.VK_S, new MoveDownCommand(networkClient));
        commands.put(KeyEvent.VK_A, new MoveLeftCommand(networkClient));
        commands.put(KeyEvent.VK_D, new MoveRightCommand(networkClient));
        commands.put(KeyEvent.VK_SPACE, new PlaceBombCommand(networkClient));
        commands.put(KeyEvent.VK_R, new ResetGameCommand(networkClient));
    }

    public void execute(int keyCode) {
        Command command = commands.get(keyCode);
        if (command != null) {
            command.execute();
        }
    }
}
