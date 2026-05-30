import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class HomeWorld extends World {
    private PopupOverlay activePopup;

    public HomeWorld() {
        super(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT, 1);
        GreenfootImage bg = loadBackground();
        setBackground(bg);
        createMenu();
    }

    private GreenfootImage loadBackground() {
        GreenfootImage image = new GreenfootImage("home_bg.png");
        
        boolean imageWidthMatchesWorldWidth = image.getWidth() == GameScreenLayout.WORLD_WIDTH;
        boolean imageHeightMatchesWorldHeight = image.getHeight() == GameScreenLayout.WORLD_HEIGHT;
        boolean imageSizeMatchesWorldSize = imageWidthMatchesWorldWidth && imageHeightMatchesWorldHeight;

        if (!imageSizeMatchesWorldSize) 
            image.scale(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT);
        
        return image;
    }

    private void createMenu() {
        int cx = GameScreenLayout.WORLD_WIDTH / 2;
        int firstY = GameScreenLayout.scale(378);
        int pitch = GameScreenLayout.scale(63);

        addMenuButton("ui/start-btn.png", "START", () -> {
                LevelManager.resetProgress();
                Greenfoot.setWorld(new LoadingWorld(() -> new IntroWorld()));
            },
            cx, firstY);
        addMenuButton("ui/settings-btn.png", "SETTINGS", this::showSettingsPopup,
            cx, firstY + pitch);
        addMenuButton("ui/instructions-btn.png", "INSTRUCTIONS", this::showInstructionsPopup,
            cx, firstY + pitch * 2);
        addMenuButton("ui/credits-btn.png", "CREDITS", this::showCreditsPopup,
            cx, firstY + pitch * 3);
    }

    /**
     * Add a menu button that uses the supplied artwork, scaled to a consistent
     * size that preserves the artwork's aspect ratio. Falls back to a drawn text
     * button if the image is missing, mirroring how the in-game controls behave.
     */
    private void addMenuButton(String imagePath, String label, Runnable action, int x, int y) {
        // Baseline button footprint (the artwork is 408×137 ≈ 2.98:1).
        int width = GameScreenLayout.scale(176);
        int height = GameScreenLayout.scale(59);

        GreenfootImage art = null;
        try {
            art = new GreenfootImage(imagePath);
        } catch (IllegalArgumentException e) {
            art = null;
        }

        if (art != null) {
            addObject(new MenuButton(art, action, width, height), x, y);
        } else {
            addObject(new MenuButton(label, action, width), x, y);
        }
    }

    private void showSettingsPopup() {
        if (activePopup != null) return;

        int cardW = GameScreenLayout.scale(560);
        int cardH = GameScreenLayout.scale(300);
        int cardX = (GameScreenLayout.WORLD_WIDTH - cardW) / 2;
        int cardY = GameScreenLayout.scale(150);
        int buttonY = cardY + cardH - GameScreenLayout.scale(132);
        int buttonW = GameScreenLayout.scale(120);
        int buttonH = GameScreenLayout.scale(34);
        int gap = GameScreenLayout.scale(14);
        int totalW = buttonW * 3 + gap * 2;
        int startX = cardX + (cardW - totalW) / 2;

        PopupOverlay.Style style = popupStyle(GameScreenLayout.scale(58), new Color(0, 0, 0, 168));

        List<PopupOverlay.Button> buttons = new ArrayList<>();
        buttons.add(new PopupOverlay.Button(
            startX,
            buttonY,
            buttonW,
            buttonH,
            overlay -> "SLOW",
            overlay -> Settings.getAnimationSpeed() == Settings.AnimationSpeed.SLOW ? Color.BLACK : Color.WHITE,
            overlay -> Settings.getAnimationSpeed() == Settings.AnimationSpeed.SLOW ? Color.WHITE : new Color(38, 46, 54),
            overlay -> {
                Settings.setAnimationSpeed(Settings.AnimationSpeed.SLOW);
                overlay.refresh();
            }));
        buttons.add(new PopupOverlay.Button(
            startX + buttonW + gap,
            buttonY,
            buttonW,
            buttonH,
            overlay -> "NORMAL",
            overlay -> Settings.getAnimationSpeed() == Settings.AnimationSpeed.NORMAL ? Color.BLACK : Color.WHITE,
            overlay -> Settings.getAnimationSpeed() == Settings.AnimationSpeed.NORMAL ? Color.WHITE : new Color(38, 46, 54),
            overlay -> {
                Settings.setAnimationSpeed(Settings.AnimationSpeed.NORMAL);
                overlay.refresh();
            }));
        buttons.add(new PopupOverlay.Button(
            startX + (buttonW + gap) * 2,
            buttonY,
            buttonW,
            buttonH,
            overlay -> "FAST",
            overlay -> Settings.getAnimationSpeed() == Settings.AnimationSpeed.FAST ? Color.BLACK : Color.WHITE,
            overlay -> Settings.getAnimationSpeed() == Settings.AnimationSpeed.FAST ? Color.WHITE : new Color(38, 46, 54),
            overlay -> {
                Settings.setAnimationSpeed(Settings.AnimationSpeed.FAST);
                overlay.refresh();
            }));
        buttons.add(new PopupOverlay.Button(
            cardX + cardW - GameScreenLayout.scale(150),
            cardY + cardH - GameScreenLayout.scale(58),
            GameScreenLayout.scale(120),
            GameScreenLayout.scale(34),
            overlay -> "BACK",
            PopupOverlay::close));

        activePopup = new PopupOverlay(
            cardW,
            cardH,
            cardX,
            cardY,
            style,
            (overlay, image) -> {
                drawPopupTitle(image, overlay, "SETTINGS", GameScreenLayout.scale(28));
                image.setFont(new Font("SansSerif", false, false, GameScreenLayout.scale(18)));
                image.setColor(Color.BLACK);
                image.drawString("Animation Speed", overlay.getCardLeft() + GameScreenLayout.scale(32), overlay.getCardTop() + GameScreenLayout.scale(116));
            },
            buttons,
            () -> activePopup = null);

        addObject(activePopup, GameScreenLayout.WORLD_WIDTH / 2, GameScreenLayout.WORLD_HEIGHT / 2);
    }

    private void showInstructionsPopup() {
        if (activePopup != null) return;

        int cardW = GameScreenLayout.scale(680);
        int cardH = GameScreenLayout.scale(500);
        int cardX = (GameScreenLayout.WORLD_WIDTH - cardW) / 2;
        int cardY = GameScreenLayout.scale(70);
        PopupOverlay.Style style = popupStyle(GameScreenLayout.scale(62), new Color(0, 0, 0, 176));

        List<PopupOverlay.Button> buttons = new ArrayList<>();
        buttons.add(new PopupOverlay.Button(
            cardX + (cardW - GameScreenLayout.scale(120)) / 2,
            cardY + cardH - GameScreenLayout.scale(60),
            GameScreenLayout.scale(120),
            GameScreenLayout.scale(34),
            overlay -> "CLOSE",
            PopupOverlay::close));

        activePopup = new PopupOverlay(
            cardW,
            cardH,
            cardX,
            cardY,
            style,
            (overlay, image) -> {
                drawPopupTitle(image, overlay, "HOW TO PLAY", GameScreenLayout.scale(30));

                int x = overlay.getCardLeft() + GameScreenLayout.scale(28);
                int y = overlay.getCardTop() + GameScreenLayout.scale(78);
                int lineH = GameScreenLayout.scale(24);
                int colGap = GameScreenLayout.scale(54);
                int colW = (overlay.getCardWidth() - GameScreenLayout.scale(56) - colGap) / 2;

                drawSection(image, "MOVEMENT COMMANDS", x, y);
                y += lineH;
                drawLine(image, "moveUp     move one tile up", x, y); y += lineH;
                drawLine(image, "moveDown   move one tile down", x, y); y += lineH;
                drawLine(image, "moveLeft   move one tile left", x, y); y += lineH;
                drawLine(image, "moveRight  move one tile right", x, y); y += lineH;

                y += GameScreenLayout.scale(18);
                drawSection(image, "CONTROLS", x, y);
                y += lineH;
                drawLine(image, "RUN     execute your script", x, y); y += lineH;
                drawLine(image, "RESET   return RIVETS home", x, y);

                int rightX = x + colW + colGap;
                int rightY = overlay.getCardTop() + GameScreenLayout.scale(132);
                drawSection(image, "LOOPS", rightX, rightY);
                rightY += lineH;
                drawLine(image, "repeat(N) {", rightX, rightY); rightY += lineH;
                drawLine(image, "  moveRight", rightX + GameScreenLayout.scale(18), rightY); rightY += lineH;
                drawLine(image, "}", rightX, rightY); rightY += lineH;

                rightY += GameScreenLayout.scale(18);
                drawSection(image, "MISSION", rightX, rightY);
                rightY += lineH;
                image.setFont(new Font("SansSerif", false, false, GameScreenLayout.scale(15)));
                image.setColor(new Color(42, 50, 58));
                image.drawString("Write code that guides RIVETS to the goal.", rightX, rightY); rightY += lineH;
                image.drawString("From Chamber 8 onward, loops improve", rightX, rightY); rightY += lineH;
                image.drawString("your score and final classification.", rightX, rightY);

                image.setColor(new Color(82, 92, 98));
                image.setFont(new Font("SansSerif", false, false, GameScreenLayout.scale(13)));
                image.drawString("Keywords are not case-sensitive: moveUp, MOVEUP, and moveup all work.", x, overlay.getCardTop() + overlay.getCardHeight() - GameScreenLayout.scale(20));
            },
            buttons,
            () -> activePopup = null);

        addObject(activePopup, GameScreenLayout.WORLD_WIDTH / 2, GameScreenLayout.WORLD_HEIGHT / 2);
    }

    private void showCreditsPopup() {
        if (activePopup != null) return;

        int cardW = GameScreenLayout.scale(520);
        int cardH = GameScreenLayout.scale(320);
        int cardX = (GameScreenLayout.WORLD_WIDTH - cardW) / 2;
        int cardY = GameScreenLayout.scale(135);
        PopupOverlay.Style style = popupStyle(GameScreenLayout.scale(62), new Color(0, 0, 0, 168));

        List<PopupOverlay.Button> buttons = new ArrayList<>();
        buttons.add(new PopupOverlay.Button(
            cardX + (cardW - GameScreenLayout.scale(120)) / 2,
            cardY + cardH - GameScreenLayout.scale(60),
            GameScreenLayout.scale(120),
            GameScreenLayout.scale(34),
            overlay -> "BACK",
            PopupOverlay::close));

        activePopup = new PopupOverlay(
            cardW,
            cardH,
            cardX,
            cardY,
            style,
            (overlay, image) -> {
                drawPopupTitle(image, overlay, "CREDITS", GameScreenLayout.scale(30));
                int left = overlay.getCardLeft() + GameScreenLayout.scale(32);
                int top = overlay.getCardTop() + GameScreenLayout.scale(128);

                image.setColor(new Color(42, 50, 58));
                image.setFont(new Font("SansSerif", false, false, GameScreenLayout.scale(18)));
                image.drawString("Created by", left + GameScreenLayout.scale(4), top - GameScreenLayout.scale(22));
                image.setFont(new Font("SansSerif", true, false, GameScreenLayout.scale(22)));
                image.drawString("Archontis E. Kostis", left - GameScreenLayout.scale(4), top + GameScreenLayout.scale(16));
                image.setFont(new Font("SansSerif", false, false, GameScreenLayout.scale(14)));
                image.drawString("Project RIVETS: CodeBot Maze", left, top + GameScreenLayout.scale(66));
            },
            buttons,
            () -> activePopup = null);

        addObject(activePopup, GameScreenLayout.WORLD_WIDTH / 2, GameScreenLayout.WORLD_HEIGHT / 2);
    }

    private PopupOverlay.Style popupStyle(int headerHeight, Color backdropColor) {
        return new PopupOverlay.Style(
            backdropColor,
            new Color(241, 242, 236),
            new Color(217, 222, 214),
            new Color(42, 50, 58),
            new Color(42, 50, 58),
            Color.WHITE,
            new Color(42, 50, 58),
            headerHeight,
            GameScreenLayout.scale(42),
            GameScreenLayout.scale(16),
            GameScreenLayout.scale(28),
            GameScreenLayout.scale(24),
            GameScreenLayout.scale(7),
            GameScreenLayout.scale(19),
            GameScreenLayout.scale(18),
            GameScreenLayout.scale(14),
            GameScreenLayout.scale(18),
            GameScreenLayout.scale(22));
    }

    private void drawPopupTitle(GreenfootImage image, PopupOverlay overlay, String title, int fontSize) {
        image.setColor(new Color(42, 50, 58));
        image.setFont(new Font("SansSerif", true, false, fontSize));
        image.drawString(title, overlay.getCardLeft() + GameScreenLayout.scale(26), overlay.getCardTop() + GameScreenLayout.scale(42));
    }

    private void drawSection(GreenfootImage image, String text, int x, int y) {
        image.setColor(new Color(76, 96, 108));
        image.setFont(new Font("SansSerif", true, false, GameScreenLayout.scale(15)));
        image.drawString(text, x, y);
    }

    private void drawLine(GreenfootImage image, String text, int x, int y) {
        image.setColor(new Color(42, 50, 58));
        image.setFont(new Font("Monospaced", false, false, GameScreenLayout.scale(15)));
        image.drawString(text, x, y);
    }

}
