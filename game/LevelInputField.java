import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple multi-line text field for the Load Custom screen — purpose-built for pasting
 * (and lightly editing) a {@code .lvl} document or base64 share-code. Unlike the in-game
 * {@link CodeEditor} it has no line numbers, STEP button, scrollbar, or syntax colouring:
 * just a panel, a placeholder, wrapped text that scrolls to show the tail, and a caret.
 *
 * <p>Editing is append-and-backspace at the end (sufficient for tweaking a paste); the
 * full document is set via {@link #setText} from the PASTE button. Long single lines
 * (base64) are soft-wrapped for display only — {@link #getText} preserves the original.
 */
public class LevelInputField extends Actor {

    private static final int FONT_SIZE = GameScreenLayout.scale(13);
    private static final int LINE_H = GameScreenLayout.scale(17);
    private static final int PAD = GameScreenLayout.scale(12);
    private static final int CHAR_W = GameScreenLayout.scale(8);
    private static final int BLINK_INTERVAL = 18;

    private static final Color PANEL_BG = new Color(18, 22, 34);
    private static final Color PANEL_BORDER = new Color(70, 90, 140);
    private static final Color TEXT_COLOR = new Color(214, 222, 235);
    private static final Color PLACEHOLDER_COLOR = new Color(110, 122, 144);
    private static final Color CARET_COLOR = new Color(200, 210, 255);
    private static final Color COUNT_COLOR = new Color(120, 134, 158);

    private final int w;
    private final int h;
    private final StringBuilder text = new StringBuilder();
    private final String placeholder;

    private int blinkTick = 0;
    private boolean caretVisible = true;

    public LevelInputField(int width, int height, String placeholder) {
        this.w = width;
        this.h = height;
        this.placeholder = placeholder;
        redraw();
    }

    public String getText() {
        return text.toString();
    }

    /** Replaces the whole field (used by the PASTE button). */
    public void setText(String newText) {
        text.setLength(0);
        if (newText != null) {
            text.append(newText);
        }
        resetBlink();
        redraw();
    }

    public void clear() {
        setText("");
    }

    @Override
    public void act() {
        String key = Greenfoot.getKey();
        if (key != null && handleKey(key)) {
            resetBlink();
        }
        tickBlink();
    }

    private boolean handleKey(String key) {
        switch (key) {
            case "backspace":
                if (text.length() > 0) {
                    text.deleteCharAt(text.length() - 1);
                    redraw();
                    return true;
                }
                return false;
            case "enter":
                text.append('\n');
                redraw();
                return true;
            case "space":
                text.append(' ');
                redraw();
                return true;
            case "tab":
                text.append("  ");
                redraw();
                return true;
            // Ignore modifier / navigation keys.
            case "shift": case "control": case "ctrl": case "alt": case "alt graph":
            case "caps lock": case "escape": case "left": case "right": case "up":
            case "down": case "home": case "end": case "page up": case "page down":
            case "insert": case "delete":
                return false;
            default:
                if (key.length() == 1) {
                    char c = key.charAt(0);
                    if (Character.isLetter(c) && Greenfoot.isKeyDown("shift")) {
                        c = Character.toUpperCase(c);
                    }
                    text.append(c);
                    redraw();
                    return true;
                }
                return false;
        }
    }

    private void resetBlink() {
        blinkTick = 0;
        caretVisible = true;
    }

    private void tickBlink() {
        blinkTick++;
        if (blinkTick >= BLINK_INTERVAL) {
            blinkTick = 0;
            caretVisible = !caretVisible;
            redraw();
        }
    }

    /** Soft-wraps each logical line to the panel width for display only. */
    private List<String> displayLines() {
        int maxChars = Math.max(1, (w - 2 * PAD) / CHAR_W);
        List<String> out = new ArrayList<>();
        for (String logical : text.toString().split("\n", -1)) {
            if (logical.isEmpty()) {
                out.add("");
                continue;
            }
            for (int i = 0; i < logical.length(); i += maxChars) {
                out.add(logical.substring(i, Math.min(logical.length(), i + maxChars)));
            }
        }
        return out;
    }

    private void redraw() {
        GreenfootImage img = new GreenfootImage(w, h);
        img.setColor(PANEL_BG);
        img.fillRect(0, 0, w, h);
        img.setColor(PANEL_BORDER);
        img.drawRect(0, 0, w - 1, h - 1);
        img.drawRect(1, 1, w - 3, h - 3);

        img.setFont(new Font("Monospaced", false, false, FONT_SIZE));

        if (text.length() == 0) {
            img.setColor(PLACEHOLDER_COLOR);
            img.drawString(placeholder, PAD, PAD + LINE_H);
            setImage(img);
            return;
        }

        List<String> lines = displayLines();
        int visible = Math.max(1, (h - 2 * PAD) / LINE_H);
        int start = Math.max(0, lines.size() - visible);

        img.setColor(TEXT_COLOR);
        int row = 0;
        int lastDrawnWidth = 0;
        for (int i = start; i < lines.size(); i++) {
            String ln = lines.get(i);
            int baseY = PAD + (row + 1) * LINE_H;
            img.drawString(ln, PAD, baseY);
            if (i == lines.size() - 1) {
                lastDrawnWidth = ln.length() * CHAR_W;
            }
            row++;
        }

        if (caretVisible) {
            int caretRow = Math.min(lines.size(), visible);
            int caretX = PAD + lastDrawnWidth;
            int caretTop = PAD + (caretRow - 1) * LINE_H + GameScreenLayout.scale(3);
            img.setColor(CARET_COLOR);
            img.fillRect(caretX, caretTop, 2, LINE_H - GameScreenLayout.scale(3));
        }

        // Character counter, bottom-right.
        img.setFont(new Font("SansSerif", false, false, GameScreenLayout.scale(11)));
        img.setColor(COUNT_COLOR);
        String count = text.length() + " chars";
        GreenfootImage countImg = new GreenfootImage(count, GameScreenLayout.scale(11), COUNT_COLOR, new Color(0, 0, 0, 0));
        img.drawImage(countImg, w - countImg.getWidth() - PAD, h - countImg.getHeight() - GameScreenLayout.scale(6));

        setImage(img);
    }
}
