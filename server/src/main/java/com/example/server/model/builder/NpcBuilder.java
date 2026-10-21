package com.example.server.model.builder;

import com.example.server.model.ServerNpc;
import com.example.server.model.ai.NpcBehaviour;

import java.util.Random;

public class NpcBuilder implements CharacterBuilder<ServerNpc> {
    private Integer id;
    private Integer x;
    private Integer y;
    private String colorHex;
    private Integer moveSpeed;
    private NpcBehaviour behaviour;
    private Random random;

    @Override
    public NpcBuilder id(int id) {
        this.id = id;
        return this;
    }

    @Override
    public NpcBuilder position(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public NpcBuilder color(String colorHex) {
        this.colorHex = colorHex;
        return this;
    }

    public NpcBuilder moveSpeed(int moveSpeed) {
        this.moveSpeed = moveSpeed;
        return this;
    }

    public NpcBuilder behaviour(NpcBehaviour behaviour) {
        this.behaviour = behaviour;
        return this;
    }

    public NpcBuilder random(Random random) {
        this.random = random;
        return this;
    }

    @Override
    public ServerNpc build() {
        if (id == null || x == null || y == null || colorHex == null
                || moveSpeed == null || behaviour == null || random == null) {
            throw new IllegalStateException("NPC builder is missing a required value");
        }
        return new ServerNpc(id, x, y, moveSpeed, colorHex,
                behaviour, behaviour.create(random), random);
    }
}
