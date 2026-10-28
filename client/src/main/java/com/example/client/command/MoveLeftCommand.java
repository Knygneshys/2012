package com.example.client.command;

import com.example.client.network.NetworkClient;

public class MoveLeftCommand extends NetworkCommand {
    public MoveLeftCommand(NetworkClient networkClient) {
        super(networkClient, "LEFT");
    }
}

