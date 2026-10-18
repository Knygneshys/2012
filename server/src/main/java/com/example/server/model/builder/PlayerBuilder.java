package com.example.server.model.builder;

import com.example.server.model.ServerPlayer;

public class PlayerBuilder implements CharacterBuilder<ServerPlayer> {
    private Integer id;
    private Integer x;
    private Integer y;
    private String colorHex;
    private String name;
    private boolean nameSet;

    @Override
    public PlayerBuilder id(int id) {
        this.id = id;
        return this;
    }

    @Override
    public PlayerBuilder position(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public PlayerBuilder color(String colorHex) {
        this.colorHex = colorHex;
        return this;
    }

    public PlayerBuilder name(String name) {
        this.name = name;
        this.nameSet = true;
        return this;
    }

    @Override
    public ServerPlayer build() {
        if (id == null || x == null || y == null || colorHex == null || !nameSet) {
            throw new IllegalStateException("Player builder is missing a required value");
        }
        return new ServerPlayer(id, name, x, y, colorHex);
    }
}
