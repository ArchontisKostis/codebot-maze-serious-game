import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Obstacle here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Obstacle extends Actor {

    public Obstacle() {
        GreenfootImage img = new GreenfootImage("floor-tiles/obstacle_1.png");
        int s = GameAreaConfig.TILE_SIZE_PX;
        if (img.getWidth() != s || img.getHeight() != s) {
            img.scale(s, s);
        }
        setImage(img);
    }
}
