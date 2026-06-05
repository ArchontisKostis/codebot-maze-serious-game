import greenfoot.*;

/**
 * Simple overlay that draws a frame around the game area.
 */
public class GameAreaFrameOverlay extends Actor {

    public GameAreaFrameOverlay() {
        int w = GameAreaConfig.GAME_AREA_WIDTH_PX;
        int h = GameAreaConfig.GAME_AREA_HEIGHT_PX;

        GreenfootImage img = new GreenfootImage(w, h);
        img.setColor(new Color(0, 0, 0, 0));
        img.fill();

        // Draw a single-pixel border using the shared UI theme color where available.
        try {
            img.setColor(UiTheme.BORDER);
        } catch (Throwable t) {
            img.setColor(new Color(120, 140, 160));
        }
        img.drawRect(0, 0, w - 1, h - 1);

        setImage(img);
    }
}
