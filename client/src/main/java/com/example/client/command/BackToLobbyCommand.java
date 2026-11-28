package com.example.client.command;

import com.example.client.network.NetworkClient;

/** Host only (enforced by the server's proxy): ends the round and returns everyone to the lobby. */
public class BackToLobbyCommand extends NetworkCommand {
    public BackToLobbyCommand(NetworkClient networkClient) {
        super(networkClient, "LOBBY");
    }
}
