/**
 * Level 7 — Sequencing Mastery: Final Challenge
 *
 * An S-curved path with 4 coins; requires all four directions and benefits from
 * mixed repeat + individual moves. The longest and most complex level.
 * Solution:  right 14, down 11, left 14, down 4, right 21
 *            (coins at (5,1), (10,1), (8,12), (11,16))
 * Teaches: composing repeat loops with different counts for each segment.
 *
 * S(1,1) → right 14 [C(5,1) C(10,1)] → (15,1)
 *        → down 11                    → (15,12)
 *        → left 14  [C(8,12)]         → (1,12)
 *        → down 4                     → (1,16)
 *        → right 21 [C(11,16)]        → (22,16) = G
 */
public class Level7 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "#S...C....C.....########",
        "###############.########",
        "###############.########",
        "###############.########",
        "###############.########",
        "###############.########",
        "###############.########",
        "###############.########",
        "###############.########",
        "###############.########",
        "###############.########",
        "#.......C.......########",
        "#.######################",
        "#.######################",
        "#.######################",
        "#..........C..........G#",
        "########################",
        "########################",
        "########################",
    };

    static {
        if (LEVEL_LINES.length != GameAreaConfig.TILE_ROWS) {
            throw new IllegalStateException(
                "Level7 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    @Override
    public void setup(MyWorld world) {
        ParsedTileLevel parsed = AsciiTileMapParser.parse(LEVEL_LINES);
        world.installTileLevel(parsed);
    }
}
