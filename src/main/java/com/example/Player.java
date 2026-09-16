package com.example;

import java.awt.Color;

public class Player extends Character {
    public String name;

    public Player(int x,int y,int moveSpeed,String name, Color color, int width, int height) {
        super(x, y, moveSpeed, color, width, height);
        this.name = name;
    }
    public void move(int dx, int dy, TileType[][] map) {
        if(position.x() + dx < 0 || position.x() + dx >= map[0].length * MapPanel.TILE_SIZE ||
           position.y() + dy < 0 || position.y() + dy >= map.length * MapPanel.TILE_SIZE) {
            return; 
        }
        GameObject.Position newPos = new Position(position.x() + dx, position.y() + dy);
        GameObject.Position corn1 = new Position(newPos.x()+width-1, newPos.y());
        GameObject.Position corn2 = new Position(newPos.x(), newPos.y()+height-1);
        GameObject.Position corn3 = new Position(newPos.x()+width-1, newPos.y()+height-1);
        int tileX = newPos.x() / MapPanel.TILE_SIZE;
        int tileY = newPos.y() / MapPanel.TILE_SIZE;

        int tileX1 = corn1.x() / MapPanel.TILE_SIZE;
        int tileY1 = corn1.y() / MapPanel.TILE_SIZE;
        int tileX2 = corn2.x() / MapPanel.TILE_SIZE;
        int tileY2 = corn2.y() / MapPanel.TILE_SIZE;
        int tileX3 = corn3.x() / MapPanel.TILE_SIZE;
        int tileY3 = corn3.y() / MapPanel.TILE_SIZE;

        if (map[tileY][tileX] != TileType.FLOOR || map[tileY1][tileX1] != TileType.FLOOR || map[tileY2][tileX2] != TileType.FLOOR || map[tileY3][tileX3] != TileType.FLOOR) {
            return; 
        }
        position = newPos;
    }
}