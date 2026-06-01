/**
 * Level 11 — Structural Reasoning: Column Descent
 *
 * A staircase path with two-dimensional regularity: move right 3
 * tiles then down 1, repeated 3 times. The structure naturally maps
 * to a nested repeat — the first nested-loop opportunity.
 *
 * Solution: (right 3, down 1) × 3
 *   1-star:  moveRight × 3, moveDown, moveRight × 3, moveDown, moveRight × 3, moveDown
 *   2-star:  flat repeat(3) for the horizontal segments, sequential moveDown calls
 *   3-star:  repeat(3) { repeat(3) { moveRight } moveDown }
 *
 * S(1,1) → staircase → G(10,4)
 *   iter 1: right 3 → (4,1), down → (4,2)
 *   iter 2: right 3 → (7,2), down → (7,3)
 *   iter 3: right 3 → (10,3), down → (10,4) = G
 */
public class Level11 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "########################",
        "########################",
        "########################",
        "########################",
        "########################",
        "###S...#################",
        "######....##############",
        "#########....###########",
        "############G###########",
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
                "Level11 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    /** Embedded {@code .lvl} document: header + the grid above, parsed by {@link LevelDocumentParser}. */
    private static final String DOCUMENT =
        "name: Column Descent\n"
        + "scorer: abstraction\n"
        + "stars: 25,75\n"
        + "---\n"
        + String.join("\n", LEVEL_LINES);

    @Override
    public void setup(SimulationWorld world) {
        world.installLevelDefinition(LevelDocumentParser.parse(DOCUMENT));
    }
}
