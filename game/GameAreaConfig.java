/**
 * Single source of truth for the playable grid: pixel size of the game area,
 * tile size, and conversions between tile indices and world pixel coordinates
 * (Greenfoot actor positions use the centre of the actor).
 *
 * The game rectangle width and height must be exact multiples of {@link #TILE_SIZE_PX}.
 */
public final class GameAreaConfig {

    /** Pixel width of the left game panel — driven by {@link GameScreenLayout}. */
    public static final int GAME_AREA_WIDTH_PX = GameScreenLayout.GAME_AREA_WIDTH_PX;
    /** Pixel height of the left game panel. */
    public static final int GAME_AREA_HEIGHT_PX = GameScreenLayout.GAME_AREA_HEIGHT_PX;

    /** Edge length of one square tile in pixels (scales with the game screen). */
    public static final int TILE_SIZE_PX = GameScreenLayout.TILE_SIZE_PX;

    public static final int TILE_COLS = GAME_AREA_WIDTH_PX / TILE_SIZE_PX;
    public static final int TILE_ROWS = GAME_AREA_HEIGHT_PX / TILE_SIZE_PX;

    static {
        if (GAME_AREA_WIDTH_PX % TILE_SIZE_PX != 0
                || GAME_AREA_HEIGHT_PX % TILE_SIZE_PX != 0) {
            throw new AssertionError("Game area must be an integer number of tiles.");
        }
    }

    private GameAreaConfig() {
    }

    /**
     * Top-left pixel X of a tile column inside the game area (world coords).
     * Matches {@link MyWorld#GAME_AREA_MIN_X} (0) for this layout.
     */
    public static int tileOriginX(int col) {
        return col * TILE_SIZE_PX;
    }

    /**
     * Top-left pixel Y of a tile row inside the game area (world coords).
     * Matches {@link MyWorld#GAME_AREA_MIN_Y} (0) for this layout.
     */
    public static int tileOriginY(int row) {
        return GameScreenLayout.HUD_STRIP_H + row * TILE_SIZE_PX;
    }

    /** Centre pixel X for placing actors on a tile. */
    public static int tileCentreX(int col) {
        return tileOriginX(col) + TILE_SIZE_PX / 2;
    }

    /** Centre pixel Y for placing actors on a tile. */
    public static int tileCentreY(int row) {
        return tileOriginY(row) + TILE_SIZE_PX / 2;
    }

    public static boolean isInsideGrid(int col, int row) {
        return col >= 0 && col < TILE_COLS && row >= 0 && row < TILE_ROWS;
    }
}
