/**
 * Level 8 — Loop Introduction: Repeat Corridor
 *
 * A straight corridor identical in length to Level 4's long march,
 * now with repeat(8) available. The "aha" moment: the same path the
 * player typed 8 individual commands for in Level 4 collapses to one
 * loop.
 *
 * Solution: right 8
 *   1-star:  moveRight × 8
 *   3-star:  repeat(8) { moveRight }
 *
 * S(1,1) → right 8 → (9,1) = G
 */
public class Level8 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "#S.......G##############",
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
        "########################",
        "########################",
    };

    static {
        if (LEVEL_LINES.length != GameAreaConfig.TILE_ROWS) {
            throw new IllegalStateException(
                "Level8 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    @Override
    public void setup(SimulationWorld world) {
        ParsedTileLevel parsed = AsciiTileMapParser.parse(LEVEL_LINES);
        world.installTileLevel(parsed);
    }
}
