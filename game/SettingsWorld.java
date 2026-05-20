import greenfoot.*;

public class SettingsWorld extends World {

    private static final int BTN_SLOW_X   = GameScreenLayout.scale(240);
    private static final int BTN_NORMAL_X = GameScreenLayout.scale(400);
    private static final int BTN_FAST_X   = GameScreenLayout.scale(560);
    private static final int BTN_Y        = GameScreenLayout.scale(340);

    public SettingsWorld() {
        super(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT, 1);
        setBackground(new GreenfootImage(
            GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT));
        drawUI();
        addButtons();
    }

    void drawUI() {
        GreenfootImage bg = getBackground();

        bg.setColor(Color.BLACK);
        bg.fill();

        // Title
        bg.setColor(Color.WHITE);
        bg.setFont(new Font(true, false, (int) Math.round(28 * GameScreenLayout.UI_SCALE)));
        bg.drawString("SETTINGS", GameScreenLayout.scale(330), GameScreenLayout.scale(110));

        // Section label
        bg.setFont(new Font((int) Math.round(18 * GameScreenLayout.UI_SCALE)));
        bg.setColor(new Color(180, 220, 255));
        bg.drawString("Animation Speed", GameScreenLayout.scale(330), GameScreenLayout.scale(260));

        // Speed indicator labels (drawn above the buttons; green = selected, grey = not)
        Settings.AnimationSpeed current = Settings.getAnimationSpeed();
        String[] labels = { "SLOW", "NORMAL", "FAST" };
        Settings.AnimationSpeed[] speeds = {
            Settings.AnimationSpeed.SLOW,
            Settings.AnimationSpeed.NORMAL,
            Settings.AnimationSpeed.FAST
        };
        int[] xs = { BTN_SLOW_X - GameScreenLayout.scale(20),
                     BTN_NORMAL_X - GameScreenLayout.scale(30),
                     BTN_FAST_X - GameScreenLayout.scale(18) };

        bg.setFont(new Font((int) Math.round(14 * GameScreenLayout.UI_SCALE)));
        for (int i = 0; i < 3; i++) {
            bg.setColor(speeds[i] == current
                ? new Color(80, 220, 80)
                : new Color(90, 90, 90));
            bg.drawString(speeds[i] == current ? "[ " + labels[i] + " ]" : labels[i],
                xs[i], BTN_Y - GameScreenLayout.scale(28));
        }
    }

    private void addButtons() {
        addObject(new MenuButton("SLOW", () -> {
            Settings.setAnimationSpeed(Settings.AnimationSpeed.SLOW);
            drawUI();
        }), BTN_SLOW_X, BTN_Y);

        addObject(new MenuButton("NORMAL", () -> {
            Settings.setAnimationSpeed(Settings.AnimationSpeed.NORMAL);
            drawUI();
        }), BTN_NORMAL_X, BTN_Y);

        addObject(new MenuButton("FAST", () -> {
            Settings.setAnimationSpeed(Settings.AnimationSpeed.FAST);
            drawUI();
        }), BTN_FAST_X, BTN_Y);

        addObject(new MenuButton("BACK", () -> Greenfoot.setWorld(new HomeWorld())),
            GameScreenLayout.scale(100), GameScreenLayout.WORLD_HEIGHT - GameScreenLayout.scale(50));
    }
}
