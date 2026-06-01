/**
 * Level 9 — Loop Introduction: Step and Repeat
 *
 * An L-shaped path. The horizontal segment is long enough to reward
 * a loop; the short vertical tail shows that loops and sequential
 * commands mix freely. Players must decide which segment to loop.
 *
 * Solution: right 5, down 2
 *   1-star:  moveRight × 5, moveDown × 2
 *   2-star:  repeat on the wrong segment (e.g. repeat(2) moveDown)
 *   3-star:  repeat(5) { moveRight }, moveDown, moveDown
 *
 * S(1,1) → right 5 → (6,1) → down 2 → (6,3) = G
 */
public class Level9 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "########################",
        "########################",
        "########################",
        "###S......##############",
        "#########.##############",
        "#########.##############",
        "#########G##############",
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
                "Level9 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    /** Embedded {@code .lvl} document: header + the grid above, parsed by {@link LevelDocumentParser}. */
    private static final String DOCUMENT =
        "name: Step and Repeat\n"
        + "scorer: abstraction\n"
        + "stars: 25,75\n"
        + "---\n"
        + String.join("\n", LEVEL_LINES);

    @Override
    public void setup(SimulationWorld world) {
        world.installLevelDefinition(LevelDocumentParser.parse(DOCUMENT));
    }
}
