/**
 * Level 1 — Straight Shot
 *
 * Open room, no obstacles. Start left, goal 5 tiles right.
 * Pedagogical intent: introduce moveRight and establish the read-type-run cycle.
 * Solution: moveRight × 5
 *
 * S at (4,10) → right 5 → (9,10) = G
 */
public class Level1 implements Level {

    /** Grid matches {@link GameAreaConfig} (24×20 with current {@link GameScreenLayout} scale). */
    private static final String[] LEVEL_LINES = {
        "########################",
        "#......................#",
        "#......................#",
        "#......................#",
        "#......................#",
        "#......................#",
        "#......................#",
        "#......................#",
        "#......................#",
        "#......................#",
        "#...S....G.............#",
        "#......................#",
        "#......................#",
        "#......................#",
        "#......................#",
        "#......................#",
        "#......................#",
        "#......................#",
        "#......................#",
        "########################",
    };

    static {
        if (LEVEL_LINES.length != GameAreaConfig.TILE_ROWS) {
            throw new IllegalStateException(
                "LEVEL_LINES has " + LEVEL_LINES.length + " rows but TILE_ROWS="
                    + GameAreaConfig.TILE_ROWS + " — fix ASCII or layout constants.");
        }
    }

    /** Embedded {@code .lvl} document: header + the grid above, parsed by {@link LevelDocumentParser}. */
    private static final String DOCUMENT =
        "name: Straight Shot\n"
        + "scorer: completion\n"
        + "---\n"
        + String.join("\n", LEVEL_LINES);

    @Override
    public void setup(SimulationWorld world) {
        world.installLevelDefinition(LevelDocumentParser.parse(DOCUMENT));
    }
}
