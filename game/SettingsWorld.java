import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class SettingsWorld here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class SettingsWorld extends World
{
    public SettingsWorld()
    {
        super(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT, 1);
        setBackground(new GreenfootImage(
            GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT));
        drawUI();
        addBackButton();
    }

    private void drawUI() {
        GreenfootImage bg = getBackground();

        bg.setColor(Color.GRAY);
        bg.fill();

        bg.setColor(Color.WHITE);
        bg.setFont(new Font((int) Math.round(30 * GameScreenLayout.UI_SCALE)));
        bg.drawString("SETTINGS", GameScreenLayout.scale(300), GameScreenLayout.scale(100));
    }

    private void addBackButton() {
        addObject(new MenuButton("BACK", () -> Greenfoot.setWorld(new HomeWorld())),
            GameScreenLayout.scale(100), GameScreenLayout.WORLD_HEIGHT - GameScreenLayout.scale(50));
    }
}
