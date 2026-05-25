import greenfoot.GreenfootImage;

/**
 * Maps {@link TileBackgroundId} to woodland tiles.
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
                return loadOrFallback("woodland_floor.png", TileBackgroundId.FLOOR);
            case WALL:
                return loadOrFallback("woodland_wall.png", TileBackgroundId.WALL);
            default:
                return createFallbackImage(id);
        }
    }

    private static GreenfootImage loadOrFallback(String fileName, TileBackgroundId id) {
        try {
            return new GreenfootImage(fileName);
        } catch (RuntimeException ex) {
            return createFallbackImage(id);
        }
    }

    private static GreenfootImage createFallbackImage(TileBackgroundId id) {
        int s = GameAreaConfig.TILE_SIZE_PX;
        GreenfootImage img = new GreenfootImage(s, s);
        switch (id) {
            case FLOOR:
                img.setColor(new greenfoot.Color(210, 210, 215));
                img.fill();
                img.setColor(new greenfoot.Color(185, 188, 198));
                img.drawRect(0, 0, s - 1, s - 1);
                break;
            case WALL:
                img.setColor(new greenfoot.Color(75, 75, 88));
                img.fill();
                img.setColor(new greenfoot.Color(45, 45, 55));
                img.drawRect(0, 0, s - 1, s - 1);
                break;
            default:
                img.setColor(greenfoot.Color.LIGHT_GRAY);
                img.fill();
                break;
        }
        return img;
    }
}
