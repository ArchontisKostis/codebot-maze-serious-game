import greenfoot.Color;
import greenfoot.GreenfootImage;

/**
 * Maps {@link TileBackgroundId} to images (procedural tiles by default).
 * Replace {@link #createImage(TileBackgroundId)} with file-based art later:
 * {@code new GreenfootImage("tiles/floor.png")} scaled to {@link GameAreaConfig#TILE_SIZE_PX}.
 */
public final class TileBackgroundRegistry {

    private TileBackgroundRegistry() {
    }

    public static GreenfootImage imageFor(TileBackgroundId id) {
        GreenfootImage img = createImage(id);
        int w = img.getWidth();
        int h = img.getHeight();
        if (w != GameAreaConfig.TILE_SIZE_PX || h != GameAreaConfig.TILE_SIZE_PX) {
            img.scale(GameAreaConfig.TILE_SIZE_PX, GameAreaConfig.TILE_SIZE_PX);
        }
        return img;
    }

    private static GreenfootImage createImage(TileBackgroundId id) {
        int s = GameAreaConfig.TILE_SIZE_PX;
        GreenfootImage img = new GreenfootImage(s, s);
        switch (id) {
            case FLOOR:
                img.setColor(new Color(210, 210, 215));
                img.fill();
                img.setColor(new Color(185, 188, 198));
                img.drawRect(0, 0, s - 1, s - 1);
                break;
            case WALL:
                img.setColor(new Color(75, 75, 88));
                img.fill();
                img.setColor(new Color(45, 45, 55));
                img.drawRect(0, 0, s - 1, s - 1);
                break;
            default:
                img.setColor(Color.LIGHT_GRAY);
                img.fill();
                break;
        }
        return img;
    }
}
