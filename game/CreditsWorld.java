import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class CreditsWorld here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class CreditsWorld extends World
{
    public CreditsWorld()
    {
        super(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT, 1);
        setBackground(new GreenfootImage(
            GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT));
        drawUI();
        addBackButton();
    }

    private void drawUI() {
        GreenfootImage bg = getBackground();

        bg.setColor(Color.BLUE);
        bg.fill();

        bg.setColor(Color.WHITE);
        bg.setFont(new Font((int) Math.round(22 * GameScreenLayout.UI_SCALE)));

        bg.drawString("CREDITS", GameScreenLayout.scale(330), GameScreenLayout.scale(100));
        bg.drawString("Created by:", GameScreenLayout.scale(300), GameScreenLayout.scale(180));
        bg.drawString("Archontis E. Kostis", GameScreenLayout.scale(260), GameScreenLayout.scale(220));
    }

    private void addBackButton() {
        addObject(new MenuButton("BACK", () -> Greenfoot.setWorld(new HomeWorld())),
            GameScreenLayout.scale(100), GameScreenLayout.WORLD_HEIGHT - GameScreenLayout.scale(50));
    }
}
