import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class IntroWorld extends World {

    // ── Layout ───────────────────────────────────────────────────────────────

    private static final int W       = GameScreenLayout.WORLD_WIDTH;
    private static final int H       = GameScreenLayout.WORLD_HEIGHT;
    private static final int IMAGE_H = H * 70 / 100;
    private static final int PANEL_Y = IMAGE_H;
    private static final int PANEL_H = H - IMAGE_H;

    // ── Palette ───────────────────────────────────────────────────────────────

    private static final Color PANEL_BG           = UiTheme.CARD_BG;
    // Translucent divider line above the narration panel — intentionally lighter
    // and semi-transparent so the scene shows through at the seam (intro-specific).
    private static final Color PANEL_BORDER       = new Color(55, 75, 95, 180);
    // Warm parchment narration text — a deliberate stylistic choice for the story
    // prose, distinct from the shared card body color.
    private static final Color TEXT_COLOR         = new Color(215, 222, 212);
    private static final Color BTN_PRIMARY        = UiTheme.BTN_PRIMARY;
    private static final Color BTN_PRIMARY_BORDER = UiTheme.BTN_PRIMARY_BORDER;
    private static final Color BTN_GHOST          = UiTheme.BTN_GHOST;
    private static final Color BTN_GHOST_BORDER   = UiTheme.BTN_GHOST_BORDER;

    // ── Pixel transition ──────────────────────────────────────────────────────

    private static final int PIXEL_SIZE      = 10;
    private static final int DISSOLVE_FRAMES = 35;

    // ── Typewriter ────────────────────────────────────────────────────────────

    private static final int CHARS_PER_FRAME = 1;

    // ── Phase state machine ───────────────────────────────────────────────────

    private enum Phase { TYPING, IDLE, DISSOLVE_OUT, DISSOLVE_IN }

    private int[] pixelOrder;
    private int   cols, rows, totalPixels;
    private Phase phase        = Phase.TYPING;
    private int   phaseFrame   = 0;
    private int   pendingSlide = -1;

    // ── State ─────────────────────────────────────────────────────────────────

    private final List<IntroSlideData> slides;
    private final GreenfootImage[]     images;
    private int            currentSlide     = 0;
    private boolean        spaceWasDown     = false;
    private int            charsVisible     = 0;
    private GreenfootImage currentSlideBase;   // scene + panel + buttons, no text
    private GreenfootImage currentSlideImage;  // base + visible text (changes each frame)

    // Hit boxes (absolute world coords), set in renderSlideBase()
    private int skipX, skipY, skipW, skipH;
    private int actionX, actionY, actionW, actionH;

    // ── Construction ──────────────────────────────────────────────────────────

    public IntroWorld() {
        super(W, H, 1);
        slides = IntroSlideRegistry.load();
        images = new GreenfootImage[slides.size()];
        for (int i = 0; i < slides.size(); i++) {
            images[i] = compositeSlideImage(slides.get(i).images);
        }

        // Build and shuffle the pixel-block grid
        cols        = (int) Math.ceil((double) W / PIXEL_SIZE);
        rows        = (int) Math.ceil((double) H / PIXEL_SIZE);
        totalPixels = cols * rows;
        pixelOrder  = new int[totalPixels];
        for (int i = 0; i < totalPixels; i++) pixelOrder[i] = i;
        for (int i = totalPixels - 1; i > 0; i--) {
            int j = Greenfoot.getRandomNumber(i + 1);
            int tmp = pixelOrder[i]; pixelOrder[i] = pixelOrder[j]; pixelOrder[j] = tmp;
        }

        currentSlideBase  = renderSlideBase(0);
        currentSlideImage = compositeText(currentSlideBase, slides.get(0).text, 0);
        setBackground(currentSlideImage);
    }

    // ── Act ───────────────────────────────────────────────────────────────────

    @Override
    public void act() {
        boolean spaceIsDown = Greenfoot.isKeyDown("space");

        switch (phase) {

            case TYPING:
                charsVisible += CHARS_PER_FRAME;
                String fullText = slides.get(currentSlide).text;
                if (charsVisible >= fullText.length()) {
                    charsVisible      = fullText.length();
                    currentSlideImage = compositeText(currentSlideBase, fullText, charsVisible);
                    setBackground(currentSlideImage);
                    phase = Phase.IDLE;
                } else {
                    currentSlideImage = compositeText(currentSlideBase, fullText, charsVisible);
                    setBackground(currentSlideImage);
                }
                // First input during typing: skip to end instead of advancing
                if ((spaceIsDown && !spaceWasDown) || Greenfoot.mouseClicked(null)) {
                    charsVisible      = fullText.length();
                    currentSlideImage = compositeText(currentSlideBase, fullText, charsVisible);
                    setBackground(currentSlideImage);
                    phase = Phase.IDLE;
                }
                break;

            case IDLE:
                if (spaceIsDown && !spaceWasDown) triggerAdvance();

                if (Greenfoot.mouseClicked(null)) {
                    MouseInfo mouse = Greenfoot.getMouseInfo();
                    if (mouse != null) {
                        int mx = mouse.getX();
                        int my = mouse.getY();
                        if (inBounds(mx, my, skipX, skipY, skipW, skipH)) {
                            startGame();
                        } else {
                            triggerAdvance();
                        }
                    }
                }
                break;

            case DISSOLVE_OUT:
                phaseFrame++;
                applyOverlay(totalPixels * phaseFrame / DISSOLVE_FRAMES);
                if (phaseFrame >= DISSOLVE_FRAMES) {
                    if (pendingSlide < 0) {
                        startGame();
                    } else {
                        currentSlide      = pendingSlide;
                        pendingSlide      = -1;
                        currentSlideBase  = renderSlideBase(currentSlide);
                        currentSlideImage = compositeText(currentSlideBase, slides.get(currentSlide).text, 0);
                        phase             = Phase.DISSOLVE_IN;
                        phaseFrame        = 0;
                        applyOverlay(totalPixels);
                    }
                }
                break;

            case DISSOLVE_IN:
                phaseFrame++;
                applyOverlay(totalPixels * (DISSOLVE_FRAMES - phaseFrame) / DISSOLVE_FRAMES);
                if (phaseFrame >= DISSOLVE_FRAMES) {
                    charsVisible      = 0;
                    currentSlideImage = compositeText(currentSlideBase, slides.get(currentSlide).text, 0);
                    setBackground(currentSlideImage);
                    phase = Phase.TYPING;
                }
                break;
        }

        spaceWasDown = spaceIsDown;
    }

    // ── Transition triggers ───────────────────────────────────────────────────

    private void triggerAdvance() {
        if (phase != Phase.IDLE) return;
        phase        = Phase.DISSOLVE_OUT;
        phaseFrame   = 0;
        pendingSlide = (currentSlide < slides.size() - 1) ? currentSlide + 1 : -1;
    }

    private void startGame() {
        Greenfoot.setWorld(new SimulationWorld(LevelManager.getCurrentLevel()));
    }

    // ── Pixel overlay ─────────────────────────────────────────────────────────

    private void applyOverlay(int count) {
        GreenfootImage frame = new GreenfootImage(currentSlideImage);
        frame.setColor(Color.BLACK);
        int cap = Math.min(count, totalPixels);
        for (int i = 0; i < cap; i++) {
            int idx = pixelOrder[i];
            frame.fillRect((idx % cols) * PIXEL_SIZE, (idx / cols) * PIXEL_SIZE,
                           PIXEL_SIZE, PIXEL_SIZE);
        }
        setBackground(frame);
    }

    // ── Slide rendering ───────────────────────────────────────────────────────

    /**
     * Composites one or more image paths into a single W×IMAGE_H frame.
     * The first image is scaled to fill. Each additional image is scaled to
     * fit (contain) and centred, so overlays with transparency sit naturally
     * on top of the background.
     */
    private static GreenfootImage compositeSlideImage(String[] paths) {
        GreenfootImage canvas = new GreenfootImage(W, IMAGE_H);
        for (int j = 0; j < paths.length; j++) {
            GreenfootImage layer = new GreenfootImage(paths[j]);
            if (j == 0) {
                layer.scale(W, IMAGE_H);
                canvas.drawImage(layer, 0, 0);
            } else {
                double scaleX = (double) W      / layer.getWidth();
                double scaleY = (double) IMAGE_H / layer.getHeight();
                double scale  = Math.min(scaleX, scaleY);
                int sw = (int)(layer.getWidth()  * scale);
                int sh = (int)(layer.getHeight() * scale);
                layer.scale(sw, sh);
                canvas.drawImage(layer, (W - sw) / 2, (IMAGE_H - sh) / 2);
            }
        }
        return canvas;
    }

    /** Renders scene + panel + buttons — no narration text. Also updates hit-box fields. */
    private GreenfootImage renderSlideBase(int index) {
        GreenfootImage bg = new GreenfootImage(W, H);
        bg.drawImage(images[index], 0, 0);
        bg.setColor(PANEL_BG);
        bg.fillRect(0, PANEL_Y, W, PANEL_H);
        bg.setColor(PANEL_BORDER);
        bg.fillRect(0, PANEL_Y, W, 2);
        drawButtons(bg, index == slides.size() - 1);
        return bg;
    }

    /** Returns a copy of base with up to charsVisible characters of text drawn on top. */
    private GreenfootImage compositeText(GreenfootImage base, String text, int charsVisible) {
        GreenfootImage frame = new GreenfootImage(base);
        drawNarrationPartial(frame, text, charsVisible);
        return frame;
    }

    // ── Narration ─────────────────────────────────────────────────────────────

    private void drawNarrationPartial(GreenfootImage bg, String text, int charsVisible) {
        int fontSize = GameScreenLayout.scale(16);
        int padX     = GameScreenLayout.scale(28);
        int lineH    = GameScreenLayout.scale(23);
        int textX    = padX;
        int textY    = PANEL_Y + GameScreenLayout.scale(22) + fontSize;
        int maxWidth = W - padX * 2;

        bg.setColor(TEXT_COLOR);
        bg.setFont(new Font("SansSerif", false, false, fontSize));

        // Wrap the full text so line layout stays stable as chars are revealed
        List<String> lines = wrapText(text, maxWidth, fontSize);
        int remaining = charsVisible;
        for (String line : lines) {
            if (remaining <= 0) break;
            bg.drawString(line.substring(0, Math.min(remaining, line.length())), textX, textY);
            textY     += lineH;
            remaining -= line.length() + 1;  // +1 for the implicit space at the line break
        }
    }

    private List<String> wrapText(String text, int maxWidth, int fontSize) {
        int charWidth    = (int)(fontSize * 0.57);
        int charsPerLine = Math.max(1, maxWidth / charWidth);
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() > 0 && current.length() + 1 + word.length() > charsPerLine) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                if (current.length() > 0) current.append(' ');
                current.append(word);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    private void drawButtons(GreenfootImage bg, boolean isLast) {
        int btnH        = GameScreenLayout.scale(32);
        int rightMargin = GameScreenLayout.scale(20);
        int gap         = GameScreenLayout.scale(10);
        int btnY        = PANEL_Y + PANEL_H - GameScreenLayout.scale(18) - btnH;

        actionW = isLast ? GameScreenLayout.scale(150) : GameScreenLayout.scale(76);
        actionH = btnH;
        actionX = W - rightMargin - actionW;
        actionY = btnY;

        skipW = GameScreenLayout.scale(60);
        skipH = btnH;
        skipX = actionX - gap - skipW;
        skipY = btnY;

        drawButton(bg, "SKIP",
            skipX, skipY, skipW, skipH, false);
        drawButton(bg, isLast ? "BEGIN TRAINING" : "NEXT ▶",
            actionX, actionY, actionW, actionH, true);
    }

    private void drawButton(GreenfootImage bg, String label,
                            int x, int y, int w, int h, boolean primary) {
        bg.setColor(primary ? BTN_PRIMARY : BTN_GHOST);
        bg.fillRect(x, y, w, h);
        bg.setColor(primary ? BTN_PRIMARY_BORDER : BTN_GHOST_BORDER);
        bg.drawRect(x, y, w - 1, h - 1);
        int fontSize = GameScreenLayout.scale(13);
        bg.setFont(new Font("SansSerif", true, false, fontSize));
        bg.setColor(Color.WHITE);
        int textW = label.length() * (int)(fontSize * 0.60);
        int textX = x + Math.max(GameScreenLayout.scale(6), (w - textW) / 2);
        int textY = y + h / 2 + fontSize / 2 - GameScreenLayout.scale(1);
        bg.drawString(label, textX, textY);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean inBounds(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
