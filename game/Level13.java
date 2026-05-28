/**
 * Level 13 — Structural Reasoning: Broken Symmetry
 *
 * A path that starts as a perfect Level-11 staircase (right 3, down 1,
 * three times) but has an irregular two-tile tail before the goal.
 * Players who apply the nested loop blindly will overshoot; they must
 * recognise where the structure breaks and append sequential commands.
 *
 * Solution: (right 3, down 1) × 3, then right 2
 *   1-star:  all moves individually
 *   2-star:  nested loop for the staircase but no tail
 *   3-star:  repeat(3) { repeat(3) { moveRight } moveDown }
 *            moveRight moveRight   (or repeat(2) { moveRight })
 *
 * S(1,1) → staircase 3× → (10,4) → right 2 → (12,4) = G
 */
public class Level13 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "#S...###################",
        "####....################",
        "#######....#############",
        "##########..G###########",
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
                "Level13 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    @Override
    public void setup(SimulationWorld world) {
        ParsedTileLevel parsed = AsciiTileMapParser.parse(LEVEL_LINES);
        world.installTileLevel(parsed);
    }
}
