import greenfoot.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PopupOverlay extends Actor {
    public interface ContentDrawer {
        void draw(PopupOverlay overlay, GreenfootImage image);
    }

    public interface ButtonAction {
        void onClick(PopupOverlay overlay);
    }

    public interface ButtonLabelProvider {
        String getLabel(PopupOverlay overlay);
    }

    public interface ButtonTextColorProvider {
        Color getColor(PopupOverlay overlay);
    }

    public interface ButtonFillColorProvider {
        Color getColor(PopupOverlay overlay);
    }

    public interface ButtonVisibilityProvider {
        boolean isVisible(PopupOverlay overlay);
    }

    public static final class Style {
        public final Color backdropColor;
        public final Color cardColor;
        public final Color headerColor;
        public final Color borderColor;
        public final Color buttonFillColor;
        public final Color buttonTextColor;
        public final Color closeColor;
        public final int headerHeight;
        public final int closeBoxMarginX;
        public final int closeBoxMarginY;
        public final int closeHitWidth;
        public final int closeHitHeight;
        public final int closeGlyphOffsetX;
        public final int closeGlyphOffsetY;
        public final int closeFontSize;
        public final int buttonFontSize;
        public final int buttonTextPaddingX;
        public final int buttonTextBaselineOffset;

        public Style(
                Color backdropColor,
                Color cardColor,
                Color headerColor,
                Color borderColor,
                Color buttonFillColor,
                Color buttonTextColor,
                Color closeColor,
                int headerHeight,
                int closeBoxMarginX,
                int closeBoxMarginY,
                int closeHitWidth,
                int closeHitHeight,
                int closeGlyphOffsetX,
                int closeGlyphOffsetY,
                int closeFontSize,
                int buttonFontSize,
                int buttonTextPaddingX,
                int buttonTextBaselineOffset) {
            this.backdropColor = backdropColor;
            this.cardColor = cardColor;
            this.headerColor = headerColor;
            this.borderColor = borderColor;
            this.buttonFillColor = buttonFillColor;
            this.buttonTextColor = buttonTextColor;
            this.closeColor = closeColor;
            this.headerHeight = headerHeight;
            this.closeBoxMarginX = closeBoxMarginX;
            this.closeBoxMarginY = closeBoxMarginY;
            this.closeHitWidth = closeHitWidth;
            this.closeHitHeight = closeHitHeight;
            this.closeGlyphOffsetX = closeGlyphOffsetX;
            this.closeGlyphOffsetY = closeGlyphOffsetY;
            this.closeFontSize = closeFontSize;
            this.buttonFontSize = buttonFontSize;
            this.buttonTextPaddingX = buttonTextPaddingX;
            this.buttonTextBaselineOffset = buttonTextBaselineOffset;
        }
    }

    public static final class Button {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final ButtonLabelProvider labelProvider;
        private final ButtonFillColorProvider fillColorProvider;
        private final ButtonTextColorProvider textColorProvider;
        private final ButtonAction action;
        private final ButtonVisibilityProvider visibilityProvider;

        public Button(int x, int y, int width, int height, ButtonLabelProvider labelProvider, ButtonAction action) {
            this(x, y, width, height, labelProvider, overlay -> null, overlay -> null, action, overlay -> true);
        }

        public Button(int x, int y, int width, int height, ButtonLabelProvider labelProvider, ButtonAction action, ButtonVisibilityProvider visibilityProvider) {
            this(x, y, width, height, labelProvider, overlay -> null, overlay -> null, action, visibilityProvider);
        }

        public Button(int x, int y, int width, int height, ButtonLabelProvider labelProvider, ButtonTextColorProvider textColorProvider, ButtonAction action) {
            this(x, y, width, height, labelProvider, overlay -> null, textColorProvider, action, overlay -> true);
        }

        public Button(int x, int y, int width, int height, ButtonLabelProvider labelProvider, ButtonTextColorProvider textColorProvider, ButtonAction action, ButtonVisibilityProvider visibilityProvider) {
            this(x, y, width, height, labelProvider, overlay -> null, textColorProvider, action, visibilityProvider);
        }

        public Button(int x, int y, int width, int height, ButtonLabelProvider labelProvider, ButtonFillColorProvider fillColorProvider, ButtonTextColorProvider textColorProvider, ButtonAction action) {
            this(x, y, width, height, labelProvider, fillColorProvider, textColorProvider, action, overlay -> true);
        }

        public Button(int x, int y, int width, int height, ButtonLabelProvider labelProvider, ButtonFillColorProvider fillColorProvider, ButtonTextColorProvider textColorProvider, ButtonAction action, ButtonVisibilityProvider visibilityProvider) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.labelProvider = labelProvider;
            this.fillColorProvider = fillColorProvider;
            this.textColorProvider = textColorProvider;
            this.action = action;
            this.visibilityProvider = visibilityProvider;
        }

        private boolean isVisible(PopupOverlay overlay) {
            return visibilityProvider.isVisible(overlay);
        }

        private boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        private void click(PopupOverlay overlay) {
            action.onClick(overlay);
        }

        private String label(PopupOverlay overlay) {
            return labelProvider.getLabel(overlay);
        }

        private Color textColor(PopupOverlay overlay) {
            Color color = textColorProvider.getColor(overlay);
            return color == null ? null : color;
        }

        private Color fillColor(PopupOverlay overlay) {
            Color color = fillColorProvider.getColor(overlay);
            return color == null ? null : color;
        }
    }

    private final int cardWidth;
    private final int cardHeight;
    private final int cardX;
    private final int cardY;
    private final Style style;
    private final ContentDrawer contentDrawer;
    private final List<Button> buttons;
    private final Runnable onClose;

    public PopupOverlay(
            int cardWidth,
            int cardHeight,
            int cardX,
            int cardY,
            Style style,
            ContentDrawer contentDrawer,
            List<Button> buttons,
            Runnable onClose) {
        this.cardWidth = cardWidth;
        this.cardHeight = cardHeight;
        this.cardX = cardX;
        this.cardY = cardY;
        this.style = style;
        this.contentDrawer = contentDrawer;
        this.buttons = buttons == null ? Collections.emptyList() : new ArrayList<>(buttons);
        this.onClose = onClose;
        redraw();
    }

    @Override
    public void act() {
        if (!Greenfoot.mouseClicked(this)) return;

        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse == null) return;

        int x = mouse.getX();
        int y = mouse.getY();
        if (inside(x, y, closeBoxX(), closeBoxY(), style.closeHitWidth, style.closeHitHeight)) {
            close();
            return;
        }

        for (Button button : buttons) {
            if (button.isVisible(this) && button.contains(x, y)) {
                button.click(this);
                return;
            }
        }
    }

    public void refresh() {
        redraw();
    }

    public int getCardLeft() {
        return cardX;
    }

    public int getCardTop() {
        return cardY;
    }

    public int getCardWidth() {
        return cardWidth;
    }

    public int getCardHeight() {
        return cardHeight;
    }

    public int getContentLeft(int padding) {
        return cardX + padding;
    }

    public int getContentTop(int padding) {
        return cardY + padding;
    }

    public int getContentWidth(int horizontalPadding) {
        return cardWidth - horizontalPadding * 2;
    }

    public int getContentHeight(int topPadding, int bottomPadding) {
        return cardHeight - topPadding - bottomPadding;
    }

    public void drawWrappedText(GreenfootImage image, String text, int x, int y, int width, int lineHeight, int maxLines, Color color) {
        int maxChars = Math.max(24, width / Math.max(1, GameScreenLayout.scale(8)));
        List<String> lines = wrap(text, maxChars);
        image.setColor(color);
        for (int i = 0; i < lines.size() && i < maxLines; i++) {
            image.drawString(lines.get(i), x, y + i * lineHeight);
        }
    }

    public void drawCodeBlock(GreenfootImage image, String text, int x, int y, int width, int height) {
        image.setColor(new Color(28, 31, 38));
        image.fillRect(x, y, width, height);
        image.setColor(new Color(106, 130, 158));
        image.drawRect(x, y, width, height);
        image.setFont(new Font("Monospaced", false, false, GameScreenLayout.scale(13)));
        image.setColor(new Color(196, 231, 201));

        String[] lines = text.split("\\n");
        int maxLines = Math.min(lines.length, 6);
        for (int i = 0; i < maxLines; i++) {
            image.drawString(lines[i], x + GameScreenLayout.scale(14), y + GameScreenLayout.scale(25) + i * GameScreenLayout.scale(19));
        }
    }

    public void drawButton(GreenfootImage image, int x, int y, int width, int height, String text) {
        image.setColor(style.buttonFillColor);
        image.fillRect(x, y, width, height);
        image.setColor(style.buttonTextColor);
        image.setFont(new Font("SansSerif", true, false, style.buttonFontSize));
        image.drawString(text, buttonTextX(x, width, text), y + style.buttonTextBaselineOffset);
    }

    public List<String> wrapText(String text, int maxChars) {
        return wrap(text, maxChars);
    }

    public void close() {
        World world = getWorld();
        if (world != null) {
            world.removeObject(this);
        }
        onClose.run();
    }

    private void redraw() {
        GreenfootImage image = new GreenfootImage(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT);
        image.setColor(style.backdropColor);
        image.fill();

        image.setColor(style.cardColor);
        image.fillRect(cardX, cardY, cardWidth, cardHeight);
        image.setColor(style.headerColor);
        image.fillRect(cardX, cardY, cardWidth, style.headerHeight);
        image.setColor(style.borderColor);
        image.drawRect(cardX, cardY, cardWidth, cardHeight);
        image.drawRect(cardX + 1, cardY + 1, cardWidth - 2, cardHeight - 2);

        contentDrawer.draw(this, image);
        drawButtons(image);
        drawCloseIcon(image);
        setImage(image);
    }

    private void drawButtons(GreenfootImage image) {
        for (Button button : buttons) {
            if (!button.isVisible(this)) {
                continue;
            }
            Color fillColor = button.fillColor(this);
            image.setColor(fillColor == null ? style.buttonFillColor : fillColor);
            image.fillRect(button.x, button.y, button.width, button.height);
            Color buttonTextColor = button.textColor(this);
            image.setColor(buttonTextColor == null ? style.buttonTextColor : buttonTextColor);
            image.setFont(new Font("SansSerif", true, false, style.buttonFontSize));
            String label = button.label(this);
            image.drawString(label, buttonTextX(button.x, button.width, label), button.y + style.buttonTextBaselineOffset);
        }
    }

    private void drawCloseIcon(GreenfootImage image) {
        image.setColor(style.closeColor);
        image.setFont(new Font("SansSerif", true, false, style.closeFontSize));
        image.drawString("X", closeBoxX() + style.closeGlyphOffsetX, closeBoxY() + style.closeGlyphOffsetY);
    }

    private int buttonTextX(int x, int width, String text) {
        int estimatedWidth = text == null ? 0 : text.length() * GameScreenLayout.scale(7);
        return x + Math.max(style.buttonTextPaddingX, (width - estimatedWidth) / 2);
    }

    private int closeBoxX() {
        return cardX + cardWidth - style.closeBoxMarginX;
    }

    private int closeBoxY() {
        return cardY + style.closeBoxMarginY;
    }

    private boolean inside(int x, int y, int left, int top, int width, int height) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }

    private List<String> wrap(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        String line = "";
        for (String word : words) {
            if (line.length() == 0) {
                line = word;
            } else if (line.length() + 1 + word.length() <= maxChars) {
                line += " " + word;
            } else {
                lines.add(line);
                line = word;
            }
        }
        if (line.length() > 0) {
            lines.add(line);
        }
        return lines;
    }
}