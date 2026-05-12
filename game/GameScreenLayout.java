/**
 * Single layout blueprint for the whole UI: world size, game vs editor vs terminal
 * proportions, and tile pixels. Change {@link #SCALE_NUM}/{@link #SCALE_DEN} to resize
 * everything together. Baseline playfield vs bottom band is {@code 500:100} (vs legacy {@code 450:150}).
 */
public final class GameScreenLayout {

    /**
     * Rational scale applied to the baseline (800×600 world, 600×500 game, 25px tiles).
     * Default {@code 6/5 = 1.2×} → world {@code 960×720}, game {@code 720×600}, tiles {@code 30px}, terminal {@code 120px} tall.
     */
    public static final int SCALE_NUM = 6;
    public static final int SCALE_DEN = 5;

    /** Font / spacing multiplier for actors that use fractional sizes. */
    public static final double UI_SCALE = (double) SCALE_NUM / SCALE_DEN;

    private GameScreenLayout() {
    }

    /** Scale a baseline pixel length from the original 800×600 design. */
    public static int scale(int basePixels) {
        return basePixels * SCALE_NUM / SCALE_DEN;
    }

    // --- Baseline-relative regions (all scale together) ---

    public static final int GAME_AREA_WIDTH_PX = scale(600);
    /** Taller playfield vs terminal strip — baseline {@code 500px} → scaled {@code 600px} at 6/5. */
    public static final int GAME_AREA_HEIGHT_PX = scale(500);
    /** Tile edge; baseline {@code 25px} → grid stays {@code 24×20} ({@code 600/25 × 500/25}). */
    public static final int TILE_SIZE_PX = scale(25);

    public static final int SCRIPT_AREA_X = GAME_AREA_WIDTH_PX;
    public static final int SCRIPT_AREA_W = scale(200);
    public static final int SCRIPT_AREA_Y = 0;
    public static final int SCRIPT_AREA_H = GAME_AREA_HEIGHT_PX;

    public static final int TERMINAL_X = 0;
    public static final int TERMINAL_Y = GAME_AREA_HEIGHT_PX;
    public static final int TERMINAL_W = GAME_AREA_WIDTH_PX;
    public static final int TERMINAL_H = scale(100);

    public static final int CONTROLS_X = GAME_AREA_WIDTH_PX;
    public static final int CONTROLS_Y = GAME_AREA_HEIGHT_PX;
    public static final int CONTROLS_W = scale(200);
    /** Same height as {@link #TERMINAL_H} so the bottom row lines up. */
    public static final int CONTROLS_H = scale(100);

    /** Total world size: (game|script) × (game|bottom). */
    public static final int WORLD_WIDTH = GAME_AREA_WIDTH_PX + SCRIPT_AREA_W;
    public static final int WORLD_HEIGHT = GAME_AREA_HEIGHT_PX + TERMINAL_H;

    /** RUN / RESET centres relative to {@link #CONTROLS_X} / {@link #CONTROLS_Y}. */
    public static final int CONTROLS_BTN_A_OFFSET_X = scale(55);
    public static final int CONTROLS_BTN_B_OFFSET_X = scale(145);
    public static final int CONTROLS_BTN_OFFSET_Y = scale(50);
}
