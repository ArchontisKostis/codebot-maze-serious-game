/**
 * Level 6 — Sequencing Mastery: Serpentine
 *
 * A three-pass serpentine path (68 moves total). Brute-force typing 68 commands
 * is tedious; the intended solution uses repeat loops for each segment.
 * Solution:  repeat 18 { right }
 *            repeat 8  { down  }
 *            repeat 18 { left  }
 *            repeat 6  { down  }
 *            repeat 18 { right }
 * Teaches: repeat loops as the natural answer to long repetitive segments.
 *
 * S(2,3) → right 18 → (20,3) → down 8 → (20,11)
 *        → left 18  → (2,11) → down 6 → (2,17)
 *        → right 18 → (20,17) = G
 */
public class Level6 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "########################",
        "########################",
        "##S..................###",
        "####################.###",
        "####################.###",
        "####################.###",
        "####################.###",
        "####################.###",
        "####################.###",
        "####################.###",
        "##...................###",
        "##.#####################",
        "##.#####################",
        "##.#####################",
        "##.#####################",
        "##.#####################",
        "##..................G###",
        "########################",
        "########################",
    };

    static {
        if (LEVEL_LINES.length != GameAreaConfig.TILE_ROWS) {
            throw new IllegalStateException(
                "Level6 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    @Override
    public void setup(MyWorld world) {
        ParsedTileLevel parsed = AsciiTileMapParser.parse(LEVEL_LINES);
        world.installTileLevel(parsed);
    }
}
