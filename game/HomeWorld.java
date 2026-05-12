import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class HomeScreenWorld here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */

public class HomeWorld extends World
{
    public HomeWorld()
    {
        super(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT, 1);
        setBackground(new GreenfootImage(
            GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT));
        drawUI();
        createMenu();
    }

    private void drawUI() {
        GreenfootImage bg = getBackground();

        bg.setColor(Color.DARK_GRAY);
        bg.fill();

        bg.setColor(Color.WHITE);
        bg.setFont(new Font((int) Math.round(36 * GameScreenLayout.UI_SCALE)));
        int tx = GameScreenLayout.scale(260);
        int ty = GameScreenLayout.scale(140);
        bg.drawString("ROBOT PROGRAMMER", tx, ty);
    }

    private void createMenu() {
        int cx = GameScreenLayout.WORLD_WIDTH / 2;
        addObject(new MenuButton("START", () -> Greenfoot.setWorld(new MyWorld( LevelManager.getCurrentLevel() ))),
            cx, GameScreenLayout.scale(300));
        addObject(new MenuButton("SETTINGS", () -> Greenfoot.setWorld(new SettingsWorld())),
            cx, GameScreenLayout.scale(370));
        addObject(new MenuButton("INSTRUCTIONS", () -> Greenfoot.setWorld(new InstructionsWorld())),
            cx, GameScreenLayout.scale(440));
        addObject(new MenuButton("CREDITS", () -> Greenfoot.setWorld(new CreditsWorld())),
            cx, GameScreenLayout.scale(510));
    }
}
