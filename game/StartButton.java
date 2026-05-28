import greenfoot.*;

/**
 * Write a description of class StartButton here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class StartButton extends Actor
{
    public StartButton() {
        setImage(new GreenfootImage("START", 30, Color.BLACK, Color.GREEN));
    }

    public void act() {
        if (Greenfoot.mouseClicked(this)) {
            Greenfoot.setWorld(new SimulationWorld( LevelManager.getCurrentLevel() ));
        }
    }
}
