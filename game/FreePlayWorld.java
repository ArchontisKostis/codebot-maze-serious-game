import greenfoot.*;

/**
 * Free Play menu reached from the home screen's FREE PLAY entry point. Offers Load
 * Custom (routes to {@link LoadCustomWorld}) plus a way back home. Campaign progress
 * is untouched.
 */
public class FreePlayWorld extends World {

    private static final int W = GameScreenLayout.WORLD_WIDTH;
    private static final int H = GameScreenLayout.WORLD_HEIGHT;

    public FreePlayWorld() {
        super(W, H, 1);
        drawBackground();

        int cx = W / 2;
        addImageButton("ui/load-custom-btn.png", "LOAD CUSTOM",
            () -> Greenfoot.setWorld(new LoadCustomWorld()), cx, GameScreenLayout.scale(350));
        addImageButton("ui/back-btn.png", "BACK",
            () -> Greenfoot.setWorld(new HomeWorld()), cx, GameScreenLayout.scale(440));
    }

    /** Adds an artwork button (same footprint for both), falling back to a text button if the image is missing. */
    private void addImageButton(String imagePath, String label, Runnable action, int x, int y) {
        int width = GameScreenLayout.scale(220);
        int height = GameScreenLayout.scale(74);
        GreenfootImage art = null;
        try {
            art = new GreenfootImage(imagePath);
        } catch (IllegalArgumentException e) {
            art = null;
        }
        if (art != null) {
            addObject(new MenuButton(art, action, width, height), x, y);
        } else {
            addObject(new MenuButton(label, action, width), x, y);
        }
    }

    private void drawBackground() {
        GreenfootImage bg = new GreenfootImage("ui/generic-bg.png");
        bg.scale(W, H);

        GreenfootImage title = new GreenfootImage("FREE PLAY",
            GameScreenLayout.scale(40), new Color(44, 220, 215), new Color(0, 0, 0, 0));
        bg.drawImage(title, (W - title.getWidth()) / 2, GameScreenLayout.scale(170));

        GreenfootImage sub = new GreenfootImage("Load a custom level to play.",
            GameScreenLayout.scale(16), new Color(170, 180, 200), new Color(0, 0, 0, 0));
        bg.drawImage(sub, (W - sub.getWidth()) / 2, GameScreenLayout.scale(225));

        setBackground(bg);
    }
}
