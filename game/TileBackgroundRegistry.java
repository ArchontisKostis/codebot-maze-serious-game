import greenfoot.GreenfootImage;

/**
 * Maps {@link TileBackgroundId} to tile artwork.
 * The source artwork is stored as pre-cropped PNGs in the images folder.
 */
public final class TileBackgroundRegistry {

    private TileBackgroundRegistry() {
    }

    public static GreenfootImage imageFor(TileBackgroundId id) {
        GreenfootImage img = createImage(id);
        if (img.getWidth() != GameAreaConfig.TILE_SIZE_PX || img.getHeight() != GameAreaConfig.TILE_SIZE_PX) {
            img.scale(GameAreaConfig.TILE_SIZE_PX, GameAreaConfig.TILE_SIZE_PX);
        }
        return img;
    }

    private static GreenfootImage createImage(TileBackgroundId id) {
        switch (id) {
            case FLOOR:
                return new GreenfootImage("game-grid-tiles/floor/floor_tile_generic.png");
            case WALL:
                return new GreenfootImage("floor-tiles/obstacle_1.png");
            default:
                throw new IllegalArgumentException("Unknown tile background: " + id);
        }
    }
}
