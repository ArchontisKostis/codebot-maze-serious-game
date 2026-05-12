/**
 * Result of loading a level definition into a {@link TileMap} plus robot start tile.
 */
public final class ParsedTileLevel {

    public final TileMap tileMap;
    public final int startCol;
    public final int startRow;

    public ParsedTileLevel(TileMap tileMap, int startCol, int startRow) {
        this.tileMap = tileMap;
        this.startCol = startCol;
        this.startRow = startRow;
    }
}
