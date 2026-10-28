package com.example.client.command;

import com.example.client.network.NetworkClient;

public class ResetGameCommand extends NetworkCommand {
    public ResetGameCommand(NetworkClient networkClient) {
        super(networkClient, "RESET");
    }
}

