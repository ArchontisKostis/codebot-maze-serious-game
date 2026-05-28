import greenfoot.Actor;
import greenfoot.Color;
import greenfoot.GreenfootImage;

/**
 * Collectible placed on a tile; removed when the robot steps on it (see {@link SimulationWorld}).
 */
public class Coin extends Actor {

    public Coin() {
        int s = GameAreaConfig.TILE_SIZE_PX;
        GreenfootImage img = new GreenfootImage(s, s);
        img.setColor(new Color(255, 220, 60));
        img.fillOval(0, 0, s - 1, s - 1);
        img.setColor(new Color(200, 160, 20));
        img.drawOval(0, 0, s - 1, s - 1);
        setImage(img);
    }
}
