package com.example;


public abstract class GameObject {
    /**
     * The position of the game object on the map in 
     * (x, y) coordinates, not tiles.
     */
   public record Position(int x, int y) {

   }
   public int width;
   public int height;
   Position position = new Position(0, 0);
   public GameObject(int x, int y, int width, int height) {
       this.position = new Position(x, y);
       this.width = width;
       this.height = height;
   }
   public Position getPosition() {
       return position;
   }
}
