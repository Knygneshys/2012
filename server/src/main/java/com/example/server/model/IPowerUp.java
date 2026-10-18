package com.example.server.model;

public interface IPowerUp {
    boolean applyTo(ServerPlayer player);
    int tileX();
    int tileY();
    int getKindId();
}
