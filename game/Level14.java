/**
 * Level 14 — Structural Reasoning: Wide Traversal
 *
 * A rectangular perimeter path. The top and bottom edges are both
 * 8 tiles wide — each rewards its own repeat block. The two vertical
 * steps connecting them are short enough to write sequentially.
 * Multiple correct 3-star decompositions exist.
 *
 * Solution: right 8, down 3, left 8
 *   1-star:  all moves individually
 *   2-star:  loops one of the two horizontal segments only
 *   3-star:  repeat(8) { moveRight }
 *            moveDown moveDown moveDown
 *            repeat(8) { moveLeft }
 *
 * S(1,1) → right 8 → (9,1) → down 3 → (9,4) → left 8 → (1,4) = G
 */
public class Level14 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "########################",
        "########################",
        "###S........############",
        "###########.############",
        "###########.############",
        "###G........############",
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
                "Level14 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    /** Embedded {@code .lvl} document: header + the grid above, parsed by {@link LevelDocumentParser}. */
    private static final String DOCUMENT =
        "name: Wide Traversal\n"
        + "scorer: abstraction\n"
        + "stars: 25,75\n"
        + "---\n"
        + String.join("\n", LEVEL_LINES);

    @Override
    public void setup(SimulationWorld world) {
        world.installLevelDefinition(LevelDocumentParser.parse(DOCUMENT));
    }
}
