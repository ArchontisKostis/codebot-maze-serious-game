import java.util.Arrays;

/**
 * Builds a {@link TileMap} from human-readable rows of characters.
 *
 * Characters:
 * <ul>
 *   <li>{@code #} — {@link TileBackgroundId#WALL} + {@link TileObjectKind#OBSTACLE}</li>
 *   <li>{@code .} — floor + empty</li>
 *   <li>{@code G} — floor + {@link TileObjectKind#GOAL}</li>
 *   <li>{@code C} — floor + {@link TileObjectKind#COIN}</li>
 *   <li>{@code S} — floor + empty; robot start (exactly one required)</li>
 *   <li>{@code space} — floor + empty (padding)</li>
 * </ul>
 *
 * Rows are padded or truncated to {@link GameAreaConfig#TILE_COLS}; if fewer than
 * {@link GameAreaConfig#TILE_ROWS} lines are provided, lower rows are filled with floor.
 */
public final class AsciiTileMapParser {

    private AsciiTileMapParser() {
    }

    public static ParsedTileLevel parse(String[] rawLines) {
        int cols = GameAreaConfig.TILE_COLS;
        int rows = GameAreaConfig.TILE_ROWS;

        if (rawLines.length > rows) {
            throw new IllegalArgumentException(
                "Level text has " + rawLines.length + " rows but the grid is only "
                    + cols + "×" + rows + ". Extra rows are ignored by mistake when this "
                    + "check is missing — drop lines or match GameAreaConfig / GameScreenLayout.");
        }

        TileMap map = new TileMap();
        int startCol = -1;
        int startRow = -1;

        for (int row = 0; row < rows; row++) {
            String line = row < rawLines.length ? rawLines[row] : "";
            char[] chars = padOrTrim(line, cols);
            for (int col = 0; col < cols; col++) {
                char ch = chars[col];
                if (ch == 'S') {
                    if (startCol >= 0) {
                        throw new IllegalArgumentException(
                            "Multiple 'S' start markers in ASCII level.");
                    }
                    map.setCell(col, row, TileBackgroundId.FLOOR, TileObjectKind.NONE);
                    startCol = col;
                    startRow = row;
                } else {
                    applyNonStartChar(map, col, row, ch);
                }
            }
        }

        if (startCol < 0) {
            throw new IllegalArgumentException(
                "ASCII level must contain exactly one 'S' start tile.");
        }

        return new ParsedTileLevel(map, startCol, startRow);
    }

    private static char[] padOrTrim(String line, int cols) {
        char[] out = new char[cols];
        Arrays.fill(out, ' ');
        int n = Math.min(line.length(), cols);
        for (int i = 0; i < n; i++) {
            out[i] = line.charAt(i);
        }
        return out;
    }

    private static void applyNonStartChar(TileMap map, int col, int row, char ch) {
        switch (ch) {
            case '#':
                map.setCell(col, row, TileBackgroundId.WALL, TileObjectKind.OBSTACLE);
                break;
            case '.':
            case ' ':
                map.setCell(col, row, TileBackgroundId.FLOOR, TileObjectKind.NONE);
                break;
            case 'G':
                map.setCell(col, row, TileBackgroundId.FLOOR, TileObjectKind.GOAL);
                break;
            case 'C':
                map.setCell(col, row, TileBackgroundId.FLOOR, TileObjectKind.COIN);
                break;
            default:
                throw new IllegalArgumentException(
                    "Unknown tile character '" + ch + "' at (" + col + "," + row + ")");
        }
    }
}
