import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Goal here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Goal extends Actor {

    public Goal() {
        int s = GameAreaConfig.TILE_SIZE_PX;
        GreenfootImage img = new GreenfootImage(s, s);
        img.setColor(new Color(40, 200, 90));
        img.fill();
        img.setColor(new Color(20, 120, 50));
        img.drawRect(0, 0, s - 1, s - 1);
        setImage(img);
    }
}
