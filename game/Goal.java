import greenfoot.Actor;
import greenfoot.Color;
import greenfoot.GreenfootImage;

public class Goal extends Actor {

    public Goal() {
        int size = GameAreaConfig.TILE_SIZE_PX;
        GreenfootImage image = new GreenfootImage(size, size);
        image.setColor(new Color(70, 160, 90));
        image.fill();
        image.setColor(new Color(35, 95, 45));
        image.drawRect(0, 0, size - 1, size - 1);
        image.setColor(Color.WHITE);
        image.drawString("G", size / 3, (size * 2) / 3);
        setImage(image);
    }
}
