import greenfoot.*;

public class InstructionsWorld extends World {

    public InstructionsWorld() {
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

        int x     = GameScreenLayout.scale(80);
        int titleY = GameScreenLayout.scale(90);
        int lineH  = GameScreenLayout.scale(36);

        bg.setColor(Color.WHITE);
        bg.setFont(new Font(true, false, (int) Math.round(26 * GameScreenLayout.UI_SCALE)));
        bg.drawString("HOW TO PLAY", x, titleY);

        bg.setFont(new Font((int) Math.round(16 * GameScreenLayout.UI_SCALE)));

        int y = titleY + GameScreenLayout.scale(50);
        bg.setColor(new Color(180, 220, 255));
        bg.drawString("Commands  (type in the code editor on the right):", x, y);

        y += lineH;
        bg.setColor(Color.WHITE);
        bg.drawString("  moveUp      — move the robot one tile up",    x, y); y += lineH;
        bg.drawString("  moveDown    — move the robot one tile down",  x, y); y += lineH;
        bg.drawString("  moveLeft    — move the robot one tile left",  x, y); y += lineH;
        bg.drawString("  moveRight   — move the robot one tile right", x, y); y += lineH;

        y += GameScreenLayout.scale(10);
        bg.setColor(new Color(180, 220, 255));
        bg.drawString("Loops:", x, y);

        y += lineH;
        bg.setColor(Color.WHITE);
        bg.drawString("  repeat(N)   — repeat the block N times (1–100)", x, y); y += lineH;
        bg.drawString("    moveRight", x, y); y += lineH;
        bg.drawString("  end         — closes the repeat block", x, y); y += lineH;

        y += GameScreenLayout.scale(10);
        bg.setColor(new Color(180, 220, 255));
        bg.drawString("Controls:", x, y);

        y += lineH;
        bg.setColor(Color.WHITE);
        bg.drawString("  RUN   — execute your script", x, y); y += lineH;
        bg.drawString("  RESET — clear the editor and restart the level", x, y); y += lineH;

        y += GameScreenLayout.scale(10);
        bg.setColor(new Color(150, 150, 150));
        bg.setFont(new Font((int) Math.round(13 * GameScreenLayout.UI_SCALE)));
        bg.drawString("Keywords are not case-sensitive: moveUp, MOVEUP, and moveup all work.", x, y);
    }

    private void addBackButton() {
        addObject(new MenuButton("BACK", () -> Greenfoot.setWorld(new HomeWorld())),
            GameScreenLayout.scale(100), GameScreenLayout.WORLD_HEIGHT - GameScreenLayout.scale(50));
    }
}
