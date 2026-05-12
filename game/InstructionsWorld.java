import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class InstructionsWorld here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class InstructionsWorld extends World
{
    public InstructionsWorld()
    {
        super(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT, 1);
        setBackground(new GreenfootImage(
            GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT));
        drawUI();
        addBackButton();
    }

    private void drawUI() {
        GreenfootImage bg = getBackground();

        bg.setColor(Color.BLACK);
        bg.fill();

        bg.setColor(Color.WHITE);
        bg.setFont(new Font((int) Math.round(22 * GameScreenLayout.UI_SCALE)));

        bg.drawString("INSTRUCTIONS", GameScreenLayout.scale(300), GameScreenLayout.scale(80));
        bg.drawString("- Use buttons to create commands", GameScreenLayout.scale(100), GameScreenLayout.scale(150));
        bg.drawString("- Press RUN to execute script", GameScreenLayout.scale(100), GameScreenLayout.scale(180));
        bg.drawString("- DEL removes last command", GameScreenLayout.scale(100), GameScreenLayout.scale(210));
        bg.drawString("- RESET clears script", GameScreenLayout.scale(100), GameScreenLayout.scale(240));
    }

    private void addBackButton() {
        addObject(new MenuButton("BACK", () -> Greenfoot.setWorld(new HomeWorld())),
            GameScreenLayout.scale(100), GameScreenLayout.WORLD_HEIGHT - GameScreenLayout.scale(50));
    }
}
