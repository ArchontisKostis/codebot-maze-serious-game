import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Obstacle here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Obstacle extends Actor {

    public Obstacle() {
        int s = GameAreaConfig.TILE_SIZE_PX;
        GreenfootImage img = new GreenfootImage(s, s);
        img.setColor(Color.RED);
        img.fill();
        img.setColor(new Color(140, 30, 30));
        img.drawRect(0, 0, s - 1, s - 1);
        setImage(img);
    }
}
