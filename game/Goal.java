import greenfoot.Actor;
import greenfoot.GreenfootImage;

public class Goal extends Actor {

    public Goal() {
        GreenfootImage img = new GreenfootImage("game-grid-tiles/general/goal-tile.png");
        int size = GameAreaConfig.TILE_SIZE_PX;
        img.scale(size, size);
        setImage(img);
    }
}
