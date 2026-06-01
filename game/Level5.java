/**
 * Level 5 — Sequencing Mastery: Coin Loop
 *
 * A square-spiral corridor with 3 coins to collect before reaching the goal.
 * Solution: right 6, down 6, left 6, down 6  (all segments equal — reward for repeat)
 * Teaches: combining coin collection with goal in a single sequence.
 *
 * S(2,2) → right 6 [C at (5,2)] → (8,2)
 *        → down 6  [C at (8,5)] → (8,8)
 *        → left 6  [C at (5,8)] → (2,8)
 *        → down 6                → (2,14) = G
 */
public class Level5 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "########################",
        "########################",
        "###S..C..###############",
        "########.###############",
        "########C###############",
        "########.###############",
        "########.###############",
        "###..C...###############",
        "###.####################",
        "###.####################",
        "###.####################",
        "###.####################",
        "###.####################",
        "###G####################",
        "########################",
        "########################",
        "########################",
        "########################",
        "########################",
    };

    static {
        if (LEVEL_LINES.length != GameAreaConfig.TILE_ROWS) {
            throw new IllegalStateException(
                "Level5 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    /** Embedded {@code .lvl} document: header + the grid above, parsed by {@link LevelDocumentParser}. */
    private static final String DOCUMENT =
        "name: Coin Loop\n"
        + "scorer: completion\n"
        + "---\n"
        + String.join("\n", LEVEL_LINES);

    @Override
    public void setup(SimulationWorld world) {
        world.installLevelDefinition(LevelDocumentParser.parse(DOCUMENT));
    }
}
