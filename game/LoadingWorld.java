import greenfoot.*;
import java.util.function.Supplier;

/**
 * Loading screen shown between major game transitions (home→intro, level→level).
 * The destination world is built lazily on the first act() frame so the splash
 * is visible before any construction work begins.
 */
public class LoadingWorld extends World
{
    private static final int    DISPLAY_TIME = 180;  // acts (~3 s at 60 fps)
    private static final int    BAR_HEIGHT   = 12;
    private static final Color  BG_COLOR     = new Color(0, 2, 45);
    private static final Color  BAR_COLOR    = new Color(236, 120, 32);
    private static final Color  TRACK_COLOR  = new Color(8, 12, 30, 210);

    private final Supplier<World> worldFactory;
    private final GreenfootImage  baseBackground;

    private int     countdown  = DISPLAY_TIME;
    private boolean worldBuilt = false;
    private World   nextWorld  = null;

    public LoadingWorld(Supplier<World> worldFactory)
    {
        super(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT, 1);
        this.worldFactory   = worldFactory;
        this.baseBackground = buildBackground();
        setBackground(baseBackground);
        drawLoadingBar();
    }

    @Override
    public void act()
    {
        if (!worldBuilt) {
            nextWorld  = worldFactory.get();
            worldBuilt = true;
        }

        drawLoadingBar();

        countdown--;
        if (countdown <= 0) {
            Greenfoot.setWorld(nextWorld);
        }
    }

    private void drawLoadingBar()
    {
        GreenfootImage frame = new GreenfootImage(baseBackground);

        int barY      = getHeight() - BAR_HEIGHT;
        int fillWidth = (int)(getWidth() * (1.0 - (countdown / (double)DISPLAY_TIME)));

        frame.setColor(TRACK_COLOR);
        frame.fillRect(0, barY, getWidth(), BAR_HEIGHT);

        frame.setColor(BAR_COLOR);
        frame.fillRect(0, barY, Math.max(0, fillWidth), BAR_HEIGHT);

        setBackground(frame);
    }

    private static GreenfootImage buildBackground()
    {
        int w = GameScreenLayout.WORLD_WIDTH;
        int h = GameScreenLayout.WORLD_HEIGHT;

        GreenfootImage bg = new GreenfootImage(w, h);
        bg.setColor(BG_COLOR);
        bg.fill();

        GreenfootImage logo = new GreenfootImage("logo/logo_dark_outline.png");
        int logoW = w / 2;
        int logoH = logo.getHeight() * logoW / logo.getWidth();
        logo.scale(logoW, logoH);

        // Centre logo horizontally and vertically in the region above the bar
        int usableH = h - BAR_HEIGHT;
        int logoX   = (w - logoW) / 2;
        int logoY   = (usableH - logoH) / 2;
        bg.drawImage(logo, logoX, logoY);

        return bg;
    }
}
