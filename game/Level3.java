/**
 * Level 3 — Four Directions
 *
 * A Z-shaped corridor that requires all four cardinal directions.
 * Solution: up 5, right 6, down 4, left 3
 * Teaches: using up/down/left/right in the same program.
 *
 * S at (4,9) → up 5 → (4,4) → right 6 → (10,4) → down 4 → (10,8) → left 3 → (7,8) = G
 */
public class Level3 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "########################",
        "########################",
        "########################",
        "####.......#############",
        "####.#####.#############",
        "####.#####.#############",
        "####.#####.#############",
        "####.##G...#############",
        "####S###################",
        "########################",
        "########################",
        "########################",
        "########################",
        "########################",
        "########################",
        "########################",
        "########################",
        "########################",
        "########################",
    };

    static {
        if (LEVEL_LINES.length != GameAreaConfig.TILE_ROWS) {
            throw new IllegalStateException(
                "Level3 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    @Override
    public void setup(SimulationWorld world) {
        ParsedTileLevel parsed = AsciiTileMapParser.parse(LEVEL_LINES);
        world.installTileLevel(parsed);
    }
}
