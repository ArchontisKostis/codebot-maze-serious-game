/**
 * Level 12 — Structural Reasoning: Stagger Grid
 *
 * A wider staircase: move right 4 tiles then down 1, repeated 4 times.
 * The visual pattern is more pronounced than Level 11 (4 rows of 4
 * instead of 3 rows of 3), requiring the player to correctly read the
 * repeating unit before coding the nested loop.
 *
 * Solution: (right 4, down 1) × 4
 *   1-star:  16 moveRight + 4 moveDown commands individually
 *   2-star:  loops one dimension but not both
 *   3-star:  repeat(4) { repeat(4) { moveRight } moveDown }
 *
 * S(1,1) → staircase → G(17,5)
 *   iter 1: right 4 → (5,1),  down → (5,2)
 *   iter 2: right 4 → (9,2),  down → (9,3)
 *   iter 3: right 4 → (13,3), down → (13,4)
 *   iter 4: right 4 → (17,4), down → (17,5) = G
 */
public class Level12 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "#S....##################",
        "#####.....##############",
        "#########.....##########",
        "#############.....######",
        "#################G######",
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
        "########################",
        "########################",
        "########################",
        "########################",
    };

    static {
        if (LEVEL_LINES.length != GameAreaConfig.TILE_ROWS) {
            throw new IllegalStateException(
                "Level12 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    @Override
    public void setup(SimulationWorld world) {
        ParsedTileLevel parsed = AsciiTileMapParser.parse(LEVEL_LINES);
        world.installTileLevel(parsed);
    }
}
