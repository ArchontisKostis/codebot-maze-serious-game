import greenfoot.GreenfootImage;
import greenfoot.World;

/**
 * Draws {@link TileMap} background tiles into the world's background image for
 * the game-area rectangle (does not touch editor / terminal / controls panels).
 */
public final class GameAreaTilePainter {

    private GameAreaTilePainter() {
    }

    public static void paintTileBackgrounds(World world, TileMap map) {
        if (map.getCols() != GameAreaConfig.TILE_COLS || map.getRows() != GameAreaConfig.TILE_ROWS) {
            throw new IllegalStateException(
                "TileMap " + map.getCols() + "×" + map.getRows() + " ≠ config "
                    + GameAreaConfig.TILE_COLS + "×" + GameAreaConfig.TILE_ROWS);
        }
        GreenfootImage bg = world.getBackground();
        for (int row = 0; row < GameAreaConfig.TILE_ROWS; row++) {
            for (int col = 0; col < GameAreaConfig.TILE_COLS; col++) {
                TileBackgroundId id = map.getCell(col, row).getBackgroundId();
                GreenfootImage tile = TileBackgroundRegistry.imageFor(id);
                int x = GameAreaConfig.tileOriginX(col);
                int y = GameAreaConfig.tileOriginY(row);
                bg.drawImage(tile, x, y);
            }
        }
    }
}
