/**
 * Level 15 — Structural Reasoning: Final Chamber
 *
 * Full synthesis. A three-row staircase where each row is 6 tiles
 * wide. The path has perfect two-dimensional regularity — the canonical
 * nested-loop structure. A purely sequential solution requires 21
 * individual commands; the optimal program is 5 lines.
 *
 * On completion this is the last level; the game returns to the Home
 * screen and the player's RIVETS classification is shown.
 *
 * Solution: (right 6, down 1) × 3
 *   1-star:  all 21 moves individually
 *   2-star:  flat repeat(6) for horizontal segments, sequential moveDown
 *   3-star:  repeat(3) { repeat(6) { moveRight } moveDown }
 *
 * S(1,1) → staircase → G(19,4)
 *   iter 1: right 6 → (7,1),  down → (7,2)
 *   iter 2: right 6 → (13,2), down → (13,3)
 *   iter 3: right 6 → (19,3), down → (19,4) = G
 */
public class Level15 implements Level {

    private static final String[] LEVEL_LINES = {
        "########################",
        "#S......################",
        "#######.......##########",
        "#############.......####",
        "###################G####",
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
                "Level15 LEVEL_LINES has " + LEVEL_LINES.length
                    + " rows but TILE_ROWS=" + GameAreaConfig.TILE_ROWS);
        }
    }

    @Override
    public void setup(MyWorld world) {
        ParsedTileLevel parsed = AsciiTileMapParser.parse(LEVEL_LINES);
        world.installTileLevel(parsed);
    }
}
