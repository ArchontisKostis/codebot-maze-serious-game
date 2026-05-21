/**
 * Level 10 — Loop Introduction: Two Segments
 *
 * A Z-shaped path with two equal horizontal runs separated by a
 * single step down. Both runs reward their own repeat block —
 * the optimal solution uses two separate loops.
 *
 * Solution: right 3, down 1, right 3
 *   1-star:  moveRight × 3, moveDown, moveRight × 3
 *   2-star:  repeat on one of the two horizontal segments only
 *   3-star:  repeat(3) { moveRight }, moveDown, repeat(3) { moveRight }
 *
 * S(2,2) → right 3 → (5,2) → down 1 → (5,3) → right 3 → (8,3) = G
 */
public class Level10 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "########################",
        "##S...##################",
        "#####...G###############",
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
        "########################",
        "########################",
    };

    static {
        if (LEVEL_LINES.length != GameAreaConfig.TILE_ROWS) {
            throw new IllegalStateException(
                "Level10 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    @Override
    public void setup(MyWorld world) {
        ParsedTileLevel parsed = AsciiTileMapParser.parse(LEVEL_LINES);
        world.installTileLevel(parsed);
    }
}
