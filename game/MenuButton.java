import greenfoot.*;

/**
 * Write a description of class MenuButton here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MenuButton extends Actor
{
    private Runnable action;

    public MenuButton(String text, Runnable action) {
        this.action = action;
        int fontSize = (int) Math.round(24 * GameScreenLayout.UI_SCALE);
        setImage(new GreenfootImage(text, fontSize, Color.BLACK, Color.LIGHT_GRAY));
    }

    public void act() {
        if (Greenfoot.mouseClicked(this)) {
            action.run();
        }
    }
}
