/**
 * Level 2 — Corridor Turn
 *
 * An L-shaped corridor. Solution: right right right down down
 * Teaches: basic two-direction sequencing.
 *
 * S at (3,5) → right 3 → (6,5) → down 2 → (6,7) = G
 */
public class Level2 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "########################",
        "########################",
        "########################",
        "########################",
        "###S...#################",
        "######.#################",
        "######G#################",
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
                "Level2 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    /** Embedded {@code .lvl} document: header + the grid above, parsed by {@link LevelDocumentParser}. */
    private static final String DOCUMENT =
        "name: Corridor Turn\n"
        + "scorer: completion\n"
        + "---\n"
        + String.join("\n", LEVEL_LINES);

    @Override
    public void setup(SimulationWorld world) {
        world.installLevelDefinition(LevelDocumentParser.parse(DOCUMENT));
    }
}
