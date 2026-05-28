/**
 * Level 4 — Long March
 *
 * A long L-shaped corridor that can be solved with 34 individual move commands,
 * but strongly rewards using repeat loops instead.
 * Solution: down 16, right 18  (or: repeat 16 { down }  repeat 18 { right })
 * Teaches: motivation for repeat loops over long command sequences.
 *
 * S at (2,1) → down 16 → (2,17) → right 18 → (20,17) = G
 */
public class Level4 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "##S#####################",
        "##.#####################",
        "##.#####################",
        "##.#####################",
        "##.#####################",
        "##.#####################",
        "##.#####################",
        "##.#####################",
        "##.#####################",
        "##.#####################",
        "##.#####################",
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
                "Level4 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    @Override
    public void setup(SimulationWorld world) {
        ParsedTileLevel parsed = AsciiTileMapParser.parse(LEVEL_LINES);
        world.installTileLevel(parsed);
    }
}
