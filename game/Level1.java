/**
 * Example level defined as ASCII art ({@link AsciiTileMapParser}).
 * Edit {@link #LEVEL_LINES} to change layout; each row must be
 * {@link GameAreaConfig#TILE_COLS} characters and there must be
 * {@link GameAreaConfig#TILE_ROWS} rows.
 */
public class Level1 implements Level {

    /** Grid matches {@link GameAreaConfig} (24×20 with current {@link GameScreenLayout} scale). */
    private static final String[] LEVEL_LINES = {
        "########################",
        "#......................#",
        "#..########............#",
        "#.......##.............#",
        "########.#.............#",
        "#......S.#.............#",
        "#..#####.#.............#",
        "#..#.....#.............#",
        "#..#.#####.............#",
        "#..#...................#",
        "#..#...CCCC..........G.#",
        "#..#...................#",
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

    @Override
    public void setup(MyWorld world) {
        ParsedTileLevel parsed = AsciiTileMapParser.parse(LEVEL_LINES);
        world.installTileLevel(parsed);
    }
}
