package com.example.client.command;

import com.example.client.network.NetworkClient;

/** Host only (enforced by the server's proxy): starts the game from the lobby. */
public class StartGameCommand extends NetworkCommand {
    public StartGameCommand(NetworkClient networkClient) {
        super(networkClient, "START");
    }
}
