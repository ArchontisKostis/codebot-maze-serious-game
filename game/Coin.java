import greenfoot.Actor;
import greenfoot.GreenfootImage;

/**
 * Collectible placed on a tile; removed when the robot steps on it (see {@link SimulationWorld}).
 */
public class Coin extends Actor {

    public Coin() {
        GreenfootImage img = new GreenfootImage("game-grid-tiles/general/coin.png");
        int size = GameAreaConfig.TILE_SIZE_PX;
        img.scale(size, size);
        setImage(img);
    }
}
