import greenfoot.*;

/**
 * FinalClassificationWorld
 *
 * Campaign finale. Composites the authored classification panel
 * ({@code final_classification_ui_element.png}) over the shared circuit-board
 * background and draws on top of it: the total-star count as orange pixel
 * digits, a 0–5 performance rating, the earned tier shield, the three tier
 * cards (earned bright, the others dimmed), a per-tier flavor message, and a
 * single HOME button.
 *
 * <p>Layout constants below are expressed in the panel artwork's native
 * coordinate space ({@value #PANEL_W}×{@value #PANEL_H}) and converted to screen
 * coordinates via the fitted {@link #scale} + centered origin
 * ({@link #sx}/{@link #sy}/{@link #sc}) — the same pattern as
 * {@link LevelCompleteOverlay}. Background compositing plus a {@link MenuButton}
 * image button mirrors {@link HomeWorld}.
 */
public class FinalClassificationWorld extends World {

    private static final int W = GameScreenLayout.WORLD_WIDTH;
    private static final int H = GameScreenLayout.WORLD_HEIGHT;

    // ── Panel artwork native dimensions + fit ───────────────────────────────────
    private static final int PANEL_W = 1177;
    private static final int PANEL_H = 648;
    /** Fraction of the world the panel may occupy (keeps aspect, leaves room for title + button). */
    private static final double PANEL_FIT = 0.88;
    /** Upward nudge from vertical center, as a fraction of world height. */
    private static final double PANEL_Y_OFFSET = 0.0;

    // ── Layout in panel-native coordinates (tune against the baked labels) ──────
    private static final int NUM_CX = 258;   // big total, centered over baked "/45"
    private static final int NUM_CY = 216;
    private static final int NUM_H = 122;    // digit height (native)
    private static final int NUM_GAP = 6;    // inter-digit gap (native)

    private static final int RATING_CX = 248;
    private static final int RATING_TOP_Y = 422;
    private static final int RATING_BOT_Y = 498;
    private static final int RATING_STAR = 66; // per-star box (native)
    private static final int RATING_STEP = 80; // horizontal spacing (native)

    private static final int SHIELD_CX = 662;
    private static final int SHIELD_CY = 326;
    private static final int SHIELD_H = 372;  // shield height (native), aspect preserved

    private static final int CARD_CX = 986;
    private static final int CARD_W = 212;    // card width (native), aspect preserved
    private static final int CARD_Y0 = 185;   // first card center
    private static final int CARD_PITCH = 122;

    private static final int FLAVOR_CX = 595;
    private static final int FLAVOR_Y1 = 556; // top of first flavor line
    private static final int FLAVOR_Y2 = 590;

    // ── World-space placement / type ────────────────────────────────────────────
    private static final int TITLE_TOP = GameScreenLayout.scale(30);
    private static final int TITLE_FS = GameScreenLayout.scale(43);
    private static final int FLAVOR_FS = GameScreenLayout.scale(18);
    private static final int BUTTON_CY = GameScreenLayout.scale(550);
    private static final int BUTTON_W = GameScreenLayout.scale(126);

    /** Light-gray title and light-blue flavor text, matched to the mockup. */
    private static final Color TITLE_COL = new Color(213, 219, 224);
    private static final Color FLAVOR_COL = new Color(120, 196, 220);
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    /** Opacity (0–255) applied to the two non-achieved tier cards. */
    private static final int DIM_CARD_ALPHA = 90;

    private final double scale;
    private final int originX;
    private final int originY;

    public FinalClassificationWorld() {
        super(W, H, 1);

        double byW = (W * PANEL_FIT) / PANEL_W;
        double byH = (H * PANEL_FIT) / PANEL_H;
        this.scale = Math.min(byW, byH);
        int panelW = (int) (PANEL_W * scale);
        int panelH = (int) (PANEL_H * scale);
        this.originX = (W - panelW) / 2;
        this.originY = (H - panelH) / 2 - (int) (H * PANEL_Y_OFFSET);

        setBackground(buildBackground());

        GreenfootImage homeArt = new GreenfootImage("ui/home-btn.png");
        int homeH = homeArt.getHeight() * BUTTON_W / homeArt.getWidth();
        addObject(new MenuButton(homeArt, () -> Greenfoot.setWorld(new HomeWorld()), BUTTON_W, homeH),
            W / 2, BUTTON_CY);
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

    private GreenfootImage buildBackground() {
        GreenfootImage bg = new GreenfootImage(W, H);
        GreenfootImage circuit = new GreenfootImage("ui/generic-bg.png");
        circuit.scale(W, H);
        bg.drawImage(circuit, 0, 0);

        drawPanel(bg);
        drawTitle(bg);

        int total = LevelManager.getTotalStars();
        int max = LevelManager.getMaxStars();
        Tier tier = tier(total);

        drawNumber(bg, total);
        drawRating(bg, total, max);
        drawShield(bg, tier);
        drawCards(bg, tier);
        drawFlavor(bg, tier);
        return bg;
    }

    private void drawPanel(GreenfootImage bg) {
        GreenfootImage panel = new GreenfootImage("ui/final_classification_ui_element.png");
        panel.scale((int) (PANEL_W * scale), (int) (PANEL_H * scale));
        bg.drawImage(panel, originX, originY);
    }

    private void drawTitle(GreenfootImage bg) {
        GreenfootImage title = new GreenfootImage("Training Complete!", TITLE_FS, TITLE_COL, TRANSPARENT);
        bg.drawImage(title, (W - title.getWidth()) / 2, TITLE_TOP);
    }

    /** Render the total as orange pixel digits ({@code num_<d>.png}), centered over the baked "/45". */
    private void drawNumber(GreenfootImage bg, int total) {
        String s = String.valueOf(total);
        int h = sc(NUM_H);
        int gap = sc(NUM_GAP);

        GreenfootImage[] digits = new GreenfootImage[s.length()];
        int totalW = 0;
        for (int i = 0; i < s.length(); i++) {
            GreenfootImage d = new GreenfootImage("ui/num_" + s.charAt(i) + ".png");
            int w = d.getWidth() * h / d.getHeight();
            d.scale(w, h);
            digits[i] = d;
            totalW += w;
        }
        totalW += gap * (s.length() - 1);

        int x = sx(NUM_CX) - totalW / 2;
        int y = sy(NUM_CY) - h / 2;
        for (GreenfootImage d : digits) {
            bg.drawImage(d, x, y);
            x += d.getWidth() + gap;
        }
    }

    /** Draw a 0–5 rating (filled vs empty stars) in a three-over-two layout. */
    private void drawRating(GreenfootImage bg, int total, int max) {
        int rating = max <= 0 ? 0 : Math.max(0, Math.min(5, Math.round(total * 5f / max)));
        int size = sc(RATING_STAR);
        int half = size / 2;

        int[] topXs = {RATING_CX - RATING_STEP, RATING_CX, RATING_CX + RATING_STEP};
        int[] botXs = {RATING_CX - RATING_STEP / 2, RATING_CX + RATING_STEP / 2};

        int slot = 0;
        for (int cx : topXs) {
            drawStar(bg, cx, RATING_TOP_Y, size, half, slot++ < rating);
        }
        for (int cx : botXs) {
            drawStar(bg, cx, RATING_BOT_Y, size, half, slot++ < rating);
        }
    }

    private void drawStar(GreenfootImage bg, int panelCx, int panelCy, int size, int half, boolean filled) {
        GreenfootImage star = new GreenfootImage(filled ? "ui/star-element.png" : "ui/no-star-element.png");
        star.scale(size, size);
        bg.drawImage(star, sx(panelCx) - half, sy(panelCy) - half);
    }

    private void drawShield(GreenfootImage bg, Tier tier) {
        GreenfootImage shield = new GreenfootImage(tier.shield);
        int h = sc(SHIELD_H);
        int w = shield.getWidth() * h / shield.getHeight();
        shield.scale(w, h);
        bg.drawImage(shield, sx(SHIELD_CX) - w / 2, sy(SHIELD_CY) - h / 2);
    }

    /** Stack the three tier cards; the achieved tier is fully opaque, the others dimmed. */
    private void drawCards(GreenfootImage bg, Tier tier) {
        String[] cards = {"ui/type_iii_card.png", "ui/type_ii_card.png", "ui/type_i_card.png"};
        int w = sc(CARD_W);
        for (int i = 0; i < cards.length; i++) {
            GreenfootImage card = new GreenfootImage(cards[i]);
            int h = card.getHeight() * w / card.getWidth();
            card.scale(w, h);
            if (i != tier.cardIndex) {
                card.setTransparency(DIM_CARD_ALPHA);
            }
            int cy = CARD_Y0 + i * CARD_PITCH;
            bg.drawImage(card, sx(CARD_CX) - w / 2, sy(cy) - h / 2);
        }
    }

    private void drawFlavor(GreenfootImage bg, Tier tier) {
        drawCenteredLine(bg, tier.flavor[0], FLAVOR_CX, FLAVOR_Y1);
        drawCenteredLine(bg, tier.flavor[1], FLAVOR_CX, FLAVOR_Y2);
    }

    private void drawCenteredLine(GreenfootImage bg, String text, int panelCx, int panelTopY) {
        GreenfootImage line = new GreenfootImage(text, FLAVOR_FS, FLAVOR_COL, TRANSPARENT);
        bg.drawImage(line, sx(panelCx) - line.getWidth() / 2, sy(panelTopY));
    }

    // ── Tier model ───────────────────────────────────────────────────────────────

    /** Resolved classification: shield art, which stacked card to highlight (−1 = none), and flavor copy. */
    private static final class Tier {
        final String shield;
        final int cardIndex;
        final String[] flavor;

        Tier(String shield, int cardIndex, String[] flavor) {
            this.shield = shield;
            this.cardIndex = cardIndex;
            this.flavor = flavor;
        }
    }

    /**
     * Map total stars to a tier. Thresholds match the baked card art (40 / 28 / 15).
     * The {@code <15} "incomplete" branch is effectively unreachable in normal play —
     * each cleared level awards at least one star — but is kept as a defensive case.
     */
    private Tier tier(int stars) {
        if (stars >= 40) {
            return new Tier("ui/type_iii_shield.png", 0, new String[]{
                "Your precision, logic, and dedication have set you apart.",
                "The future of robotics is in good hands."});
        }
        if (stars >= 28) {
            return new Tier("ui/type_ii_shield.png", 1, new String[]{
                "Strong, reliable control with real optimisation instincts.",
                "A little more refinement and full mastery is within reach."});
        }
        if (stars >= 15) {
            return new Tier("ui/type_i_shield.png", 2, new String[]{
                "You guided RIVETS home and proved the fundamentals.",
                "Tighten your logic with loops to climb the ranks."});
        }
        return new Tier("ui/incomplete-shield.png", -1, new String[]{
            "RIVETS reached the final gate, but the record falls short.",
            "Regroup, refine your code, and run the campaign again."});
    }
}
