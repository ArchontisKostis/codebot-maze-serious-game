/**
 * Fixed-size 2D grid aligned to {@link GameAreaConfig}. Used for rendering,
 * movement rules, and win/collectible logic — not pixel collisions.
 */
public final class TileMap {

    private final int cols;
    private final int rows;
    private final TileCell[][] cells;

    /**
     * Grid size always matches {@link GameAreaConfig} — no constructor parameters so row/column
     * counts cannot be accidentally swapped or drift from the layout.
     */
    public TileMap() {
        this.cols = GameAreaConfig.TILE_COLS;
        this.rows = GameAreaConfig.TILE_ROWS;
        this.cells = new TileCell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new TileCell(TileBackgroundId.FLOOR, TileObjectKind.NONE);
            }
        }
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public TileCell getCell(int col, int row) {
        return cells[row][col];
    }

    public void setCell(int col, int row, TileBackgroundId bg, TileObjectKind object) {
        cells[row][col] = new TileCell(bg, object);
    }

    /**
     * Whether the actor may enter this tile (edge of map and obstacles block).
     */
    public boolean isWalkable(int col, int row) {
        if (!GameAreaConfig.isInsideGrid(col, row)) {
            return false;
        }
        TileObjectKind o = getCell(col, row).getObjectKind();
        return o != TileObjectKind.OBSTACLE;
    }

    public boolean isGoalAt(int col, int row) {
        return GameAreaConfig.isInsideGrid(col, row)
            && getCell(col, row).getObjectKind() == TileObjectKind.GOAL;
    }

    public boolean isCoinAt(int col, int row) {
        return GameAreaConfig.isInsideGrid(col, row)
            && getCell(col, row).getObjectKind() == TileObjectKind.COIN;
    }
}
