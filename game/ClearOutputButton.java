import greenfoot.Actor;
import greenfoot.Color;
import greenfoot.Greenfoot;
import greenfoot.GreenfootImage;

public class ClearOutputButton extends Actor {

    private final Runnable onClick;
    private static final int SIZE = GameScreenLayout.scale(22);

    public ClearOutputButton(Runnable onClick) {
        this.onClick = onClick;
        setImage(createImage());
    }

    @Override
    public void act() {
        if (Greenfoot.mouseClicked(this)) {
            onClick.run();
        }
    }

    private GreenfootImage createImage() {
        GreenfootImage image = new GreenfootImage(SIZE, SIZE);

        image.setColor(new Color(238, 239, 232));
        image.fillRect(0, 0, SIZE, SIZE);
        image.setColor(new Color(42, 50, 58));
        image.drawRect(0, 0, SIZE - 1, SIZE - 1);

        int binX = GameScreenLayout.scale(7);
        int binY = GameScreenLayout.scale(7);
        int binW = GameScreenLayout.scale(8);
        int binH = GameScreenLayout.scale(10);
        image.drawLine(binX - 1, binY, binX + binW, binY);
        image.drawLine(binX + 1, binY - GameScreenLayout.scale(2), binX + binW - 2, binY - GameScreenLayout.scale(2));
        image.drawLine(binX + 3, binY - GameScreenLayout.scale(3), binX + binW - 4, binY - GameScreenLayout.scale(3));
        image.drawRect(binX, binY + 1, binW - 1, binH);
        image.drawLine(binX + 2, binY + 3, binX + 2, binY + binH - 1);
        image.drawLine(binX + binW - 3, binY + 3, binX + binW - 3, binY + binH - 1);
        return image;
    }
}