import greenfoot.*;

public class MenuButton extends Actor {
    private final Runnable action;
    private final int fixedWidth;
    private String clickSound = Sfx.BUTTON_CLICK;

    public MenuButton(String text, Runnable action) {
        this(text, action, GameScreenLayout.scale(156));
    }

    public MenuButton(String text, Runnable action, int width) {
        this.action = action;
        this.fixedWidth = width;
        setImage(createImage(text));
    }

    /** Construct a button from an existing image, scaled to the given size. */
    public MenuButton(GreenfootImage art, Runnable action, int width, int height) {
        this.action = action;
        this.fixedWidth = width;
        GreenfootImage imgCopy = new GreenfootImage(art);
        imgCopy.scale(width, height);
        setImage(imgCopy);
    }

    /** Construct a button by loading an image filename (path relative to scenario). */
    public MenuButton(String imagePath, Runnable action, int width, int height, boolean isImage) {
        this.action = action;
        this.fixedWidth = width;
        GreenfootImage img = null;
        try {
            img = new GreenfootImage(imagePath);
        } catch (IllegalArgumentException e) {
            img = null;
        }
        if (img != null) {
            img.scale(width, height);
            setImage(img);
        } else {
            setImage(createImage(""));
        }
    }

    /** Override the click sound for this button (e.g. RUN/RESET). Returns this for chaining. */
    public MenuButton withClickSound(String soundFile) {
        this.clickSound = soundFile;
        return this;
    }

    @Override
    public void act() {
        if (Greenfoot.mouseClicked(this)) {
            Sfx.play(clickSound);
            action.run();
        }
    }

    private GreenfootImage createImage(String text) {
        int fontSize = GameScreenLayout.scale(16);
        int width = fixedWidth;
        int height = GameScreenLayout.scale(36);
        GreenfootImage image = new GreenfootImage(width, height);

        image.setColor(UiTheme.BTN_PRIMARY);
        image.fillRect(0, 0, width, height);
        image.setColor(UiTheme.BTN_PRIMARY_BORDER);
        image.drawRect(0, 0, width - 1, height - 1);
        image.drawRect(1, 1, width - 3, height - 3);

        image.setColor(UiTheme.BTN_TEXT);
        image.setFont(new Font("SansSerif", true, false, fontSize));
        // Render the text to a temporary image so we can measure it exactly,
        // then draw that image centered in the button.
        GreenfootImage textImg = new GreenfootImage(text, fontSize, UiTheme.BTN_TEXT, new Color(0,0,0,0));
        int textDrawX = (width - textImg.getWidth()) / 2;
        int textDrawY = (height - textImg.getHeight()) / 2;

        if ("RUN".equals(text)) {
            int iconX = GameScreenLayout.scale(14);
            int iconY = GameScreenLayout.scale(10);
            drawRunIcon(image, iconX, iconY);
            // Shift the text a bit to the right of the icon but keep the whole label visually centered
            int shiftedX = Math.max(textDrawX, iconX + GameScreenLayout.scale(20));
            image.drawImage(textImg, shiftedX, textDrawY);
        } else {
            image.drawImage(textImg, textDrawX, textDrawY);
        }
        return image;
    }

    private void drawRunIcon(GreenfootImage image, int x, int y) {
        int size = GameScreenLayout.scale(16);
        int[] xPoints = { x, x, x + size };
        int[] yPoints = { y, y + size, y + size / 2 };

        image.setColor(new Color(56, 168, 94));
        image.fillPolygon(xPoints, yPoints, 3);
        image.setColor(new Color(24, 96, 52));
        image.drawPolygon(xPoints, yPoints, 3);
    }

    private int buttonWidth(String text) {
        return fixedWidth;
    }

    private int textX(String text, int width) {
        int approxTextWidth = text.length() * GameScreenLayout.scale(9);
        return Math.max(GameScreenLayout.scale(10), (width - approxTextWidth) / 2);
    }
}
