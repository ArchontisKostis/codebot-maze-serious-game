import greenfoot.*;

/**
 * LevelCompleteOverlay
 *
 * Full-screen overlay shown after the win-pause when a level is completed.
 * Renders {@code level-compete-panel.png} (scaled to fit the world) and draws
 * on top of it: the earned star rating, the live LEVEL / ATTEMPTS values, and
 * three clickable buttons (HOME / REPLAY / NEXT LEVEL).
 *
 * Modeled on {@link OnboardOverlay}: a single Actor that builds one image and
 * maps mouse clicks to button hit-boxes.
 *
 * <p>Layout constants below are expressed in the panel artwork's native
 * coordinate space ({@value #PANEL_W}×{@value #PANEL_H}) and converted to
 * screen coordinates via the fitted {@code scale} + centered origin. Adjust
 * these to nudge elements over the baked-in panel labels.
 */
public class LevelCompleteOverlay extends Actor {

    private static final int W = GameScreenLayout.WORLD_WIDTH;
    private static final int H = GameScreenLayout.WORLD_HEIGHT;

    private static final Color BACKDROP = new Color(0, 0, 0, 180);
    private static final Color NUMBER_COL = new Color(241, 242, 236);

    // ── Panel artwork native dimensions ────────────────────────────────────────
    private static final int PANEL_W = 913;
    private static final int PANEL_H = 809;

    // Fraction of the world the panel is allowed to occupy (smaller = more compact).
    private static final double PANEL_FIT = 0.80;

    // Upward nudge from vertical center, as a fraction of world height.
    private static final double PANEL_Y_OFFSET = 0.035;

    // ── Layout in panel-native coordinates (tune against the baked labels) ──────
    private static final int[] COLUMN_X = {238, 453, 674}; // three-up column centers
    private static final int STAR_ROW_Y = 350;             // RIVET box interior
    private static final int STAR_BOX_W = 120;             // per-star draw box (native)
    private static final int STAR_BOX_H = 114;
    private static final int NUMBER_ROW_Y = 612;           // big LEVEL / ATTEMPTS values
    private static final int BUTTON_ROW_Y = 706;           // HOME / REPLAY / NEXT row
    private static final int BUTTON_TARGET_H = 62;         // on-screen button height (native)

    // ── State ───────────────────────────────────────────────────────────────────
    private final SimulationWorld world;
    private final int stars;
    private final int levelNumber;
    private final int attempts;
    private final boolean customMode;

    private final double scale;
    private final int originX;
    private final int originY;

    // Button hit-boxes in screen coordinates: {x, y, w, h}
    private int[] homeBox;
    private int[] replayBox;
    private int[] nextBox;

    public LevelCompleteOverlay(SimulationWorld world, int stars, int levelNumber, int attempts) {
        this(world, stars, levelNumber, attempts, false);
    }

    public LevelCompleteOverlay(SimulationWorld world, int stars, int levelNumber, int attempts, boolean customMode) {
        this.world = world;
        this.stars = Math.max(0, Math.min(3, stars));
        this.levelNumber = levelNumber;
        this.attempts = attempts;
        this.customMode = customMode;

        // Fit the panel inside the world, keeping aspect ratio with a small margin.
        double byW = (W * PANEL_FIT) / PANEL_W;
        double byH = (H * PANEL_FIT) / PANEL_H;
        this.scale = Math.min(byW, byH);
        int panelW = (int) (PANEL_W * scale);
        int panelH = (int) (PANEL_H * scale);
        this.originX = (W - panelW) / 2;
        this.originY = (H - panelH) / 2 - (int) (H * PANEL_Y_OFFSET);

        world.setLevelCompleteActive(true);
        redraw();
    }

    // ── Coordinate helpers (panel-native → screen) ──────────────────────────────

    private int sx(int panelX) {
        return originX + (int) (panelX * scale);
    }

    private int sy(int panelY) {
        return originY + (int) (panelY * scale);
    }

    private int sc(int panelLen) {
        return (int) (panelLen * scale);
    }

    // ── Rendering ────────────────────────────────────────────────────────────────

    private void redraw() {
        GreenfootImage img = new GreenfootImage(W, H);
        img.setColor(BACKDROP);
        img.fill();

        drawPanel(img);
        drawStars(img);
        drawNumbers(img);
        drawButtons(img);

        setImage(img);
    }

    private void drawPanel(GreenfootImage img) {
        GreenfootImage panel = new GreenfootImage("ui/level-compete-panel.png");
        panel.scale((int) (PANEL_W * scale), (int) (PANEL_H * scale));
        img.drawImage(panel, originX, originY);
    }

    private void drawStars(GreenfootImage img) {
        int boxW = sc(STAR_BOX_W);
        int boxH = sc(STAR_BOX_H);
        for (int i = 0; i < 3; i++) {
            GreenfootImage star = new GreenfootImage(
                i < stars ? "ui/star-element.png" : "ui/no-star-element.png");
            star.scale(boxW, boxH);
            img.drawImage(star, sx(COLUMN_X[i]) - boxW / 2, sy(STAR_ROW_Y) - boxH / 2);
        }
    }

    private void drawNumbers(GreenfootImage img) {
        // LEVEL value (column 0) and ATTEMPTS value (column 2); SCORE (column 1)
        // is intentionally left blank. Custom levels have no campaign number.
        if (!customMode) {
            drawCenteredNumber(img, String.valueOf(levelNumber), COLUMN_X[0], NUMBER_ROW_Y);
        }
        drawCenteredNumber(img, String.valueOf(attempts), COLUMN_X[2], NUMBER_ROW_Y);
    }

    private void drawCenteredNumber(GreenfootImage img, String text, int panelX, int panelY) {
        int fontSize = sc(34);
        GreenfootImage num = new GreenfootImage(text, fontSize, NUMBER_COL, new Color(0, 0, 0, 0));
        img.drawImage(num, sx(panelX) - num.getWidth() / 2, sy(panelY) - num.getHeight() / 2);
    }

    private void drawButtons(GreenfootImage img) {
        if (customMode) {
            // Free Play: only Replay and Back-to-Free-Play (no campaign Home/Next).
            replayBox = drawButton(img, "ui/replay-btn.png", COLUMN_X[0]);
            homeBox = drawButton(img, "ui/home-btn.png", COLUMN_X[2]);
            nextBox = null;
            return;
        }
        // On the final level there is no next level: the third button takes the
        // player to the final-results screen instead of advancing.
        String nextAsset = LevelManager.hasNextLevel()
            ? "ui/next-lvl-btn.png"
            : "ui/final-results-btn.png";
        homeBox = drawButton(img, "ui/home-btn.png", COLUMN_X[0]);
        replayBox = drawButton(img, "ui/replay-btn.png", COLUMN_X[1]);
        nextBox = drawButton(img, nextAsset, COLUMN_X[2]);
    }

    private int[] drawButton(GreenfootImage img, String asset, int panelColumnX) {
        GreenfootImage btn = new GreenfootImage(asset);
        int targetH = sc(BUTTON_TARGET_H);
        int targetW = btn.getWidth() * targetH / btn.getHeight();
        btn.scale(targetW, targetH);
        int x = sx(panelColumnX) - targetW / 2;
        int y = sy(BUTTON_ROW_Y) - targetH / 2;
        img.drawImage(btn, x, y);
        return new int[]{x, y, targetW, targetH};
    }

    // ── Input ────────────────────────────────────────────────────────────────────

    @Override
    public void act() {
        if (!Greenfoot.mouseClicked(this)) return;
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse == null) return;
        int mx = mouse.getX();
        int my = mouse.getY();

        if (inside(mx, my, homeBox)) {
            Sfx.buttonClick();
            if (customMode) {
                world.goToFreePlay();
            } else {
                world.goHome();
            }
        } else if (inside(mx, my, replayBox)) {
            Sfx.buttonClick();
            world.replayLevel();
        } else if (nextBox != null && inside(mx, my, nextBox)) {
            Sfx.buttonClick();
            world.advanceToNextLevel();
        }
    }

    private boolean inside(int mx, int my, int[] box) {
        return box != null
            && mx >= box[0] && mx <= box[0] + box[2]
            && my >= box[1] && my <= box[1] + box[3];
    }
}
