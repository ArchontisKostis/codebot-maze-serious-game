import greenfoot.*;
import java.util.List;

/**
 * CodeEditor
 *
 * A Greenfoot Actor that acts as a simple multi-line code editor.
 * Size matches {@link GameScreenLayout#SCRIPT_AREA_W} × {@link GameScreenLayout#SCRIPT_AREA_H}.
 *
 * Key handling:
 *   - Letters           → lowercase; uppercase when shift held
 *   - Digits 0-9        → digit; shift+9 → '(', shift+0 → ')'
 *   - enter             → newline
 *   - backspace         → delete char before cursor
 *   - delete            → delete char after cursor (forward delete)
 *   - left/right/up/down→ move cursor (up/down keep a goal column across short lines)
 *   - home/end          → start / end of current line
 *   - space             → space
 *   - tab               → two spaces (indentation)
 *
 * Note on Greenfoot key names:
 *   Greenfoot.getKey() returns the KEY_PRESSED name, not the typed character.
 *   Digits come back as "0"-"9" regardless of shift. We detect shift state
 *   via Greenfoot.isKeyDown("shift") to produce '(' and ')'.
 *   Some Greenfoot builds return "(" directly for shift+9; we handle both.
 */
public class CodeEditor extends Actor {

    // ── Layout ────────────────────────────────────────────────────────────────

    private static final int W          = GameScreenLayout.SCRIPT_AREA_W;
    private static final int H          = GameScreenLayout.SCRIPT_AREA_H;
    /** Original editor used 12 / 20 / 16 / 26 / 6 / 7 — scaled with {@link GameScreenLayout#UI_SCALE}. */
    private static final int FONT_SIZE  = (int) Math.round(12 * GameScreenLayout.UI_SCALE);
    private static final int TITLE_H    = (int) Math.round(20 * GameScreenLayout.UI_SCALE);
    private static final int LINE_H     = (int) Math.round(16 * GameScreenLayout.UI_SCALE);
    private static final int PAD_LEFT   = (int) Math.round(26 * GameScreenLayout.UI_SCALE);
    private static final int PAD_TOP    = (int) Math.round(6 * GameScreenLayout.UI_SCALE);
    private static final int CHAR_W     = (int) Math.round(7 * GameScreenLayout.UI_SCALE);

    /** Toggles every {@link #BLINK_INTERVAL} acts when the user is idle so the caret blinks. */
    private static final int BLINK_INTERVAL = 18;

    // ── State ─────────────────────────────────────────────────────────────────

    private final StringBuilder text = new StringBuilder();
    private int cursorPos = 0;   // character offset into text

    /**
     * Column index used for up/down movement (sticky goal column). Updated on horizontal moves
     * and typing, not when moving vertically — so short lines do not reset the column forever.
     */
    private int goalColumn = 0;

    private int  blinkTick    = 0;
    private boolean caretVisible = true;

    // ── Constructor ───────────────────────────────────────────────────────────

    public CodeEditor() {
        redraw();
    }

    // ── Greenfoot lifecycle ───────────────────────────────────────────────────

    @Override
    public void act() {
        String key = Greenfoot.getKey();
        if (key != null && handleKey(key)) {
            resetCaretBlink();
        }
        tickCaretBlink();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Returns the current editor contents as a single string. */
    public String getText() {
        return text.toString();
    }

    /** Clears all text and resets the cursor. */
    public void clear() {
        text.setLength(0);
        cursorPos = 0;
        goalColumn = 0;
        resetCaretBlink();
        redraw();
    }

    // ── Key handling ──────────────────────────────────────────────────────────

    /** @return true if text or cursor changed (caret blink reset by caller). */
    private boolean handleKey(String key) {
        switch (key) {
            // ── Cursor movement ───────────────────────────────────────────────
            case "left":
                return moveLeft();

            case "right":
                return moveRight();

            case "up":
                return moveUp();

            case "down":
                return moveDown();

            case "home":
                return moveLineStart();

            case "end":
                return moveLineEnd();

            // ── Control keys ──────────────────────────────────────────────────
            case "backspace":
                if (cursorPos > 0) {
                    text.deleteCharAt(cursorPos - 1);
                    cursorPos--;
                    syncGoalColumnFromCursor();
                    redraw();
                    return true;
                }
                return false;

            case "enter":
                text.insert(cursorPos, '\n');
                cursorPos++;
                syncGoalColumnFromCursor();
                redraw();
                return true;

            case "space":
                text.insert(cursorPos, ' ');
                cursorPos++;
                syncGoalColumnFromCursor();
                redraw();
                return true;

            case "tab":
                text.insert(cursorPos, "  ");   // two-space indent
                cursorPos += 2;
                syncGoalColumnFromCursor();
                redraw();
                return true;

            case "delete":
            case "del":
                if (cursorPos < text.length()) {
                    text.deleteCharAt(cursorPos);
                    redraw();
                    return true;
                }
                return false;

            // ── Digits (shift+9 → '(', shift+0 → ')') ────────────────────────
            case "9":
                insertChar(Greenfoot.isKeyDown("shift") ? '(' : '9');
                return true;

            case "0":
                insertChar(Greenfoot.isKeyDown("shift") ? ')' : '0');
                return true;

            // ── Direct paren returns (some Greenfoot builds) ──────────────────
            case "(":
                insertChar('(');
                return true;

            case ")":
                insertChar(')');
                return true;

            // ── Everything else ───────────────────────────────────────────────
            default:
                if (key.length() == 1) {
                    char c = key.charAt(0);
                    if (Character.isLetter(c)) {
                        insertChar(Greenfoot.isKeyDown("shift")
                            ? Character.toUpperCase(c) : c);
                        return true;
                    }
                    if (Character.isDigit(c)) {
                        insertChar(c);
                        return true;
                    }
                }
                return false;
        }
    }

    private void insertChar(char c) {
        text.insert(cursorPos, c);
        cursorPos++;
        syncGoalColumnFromCursor();
        redraw();
    }

    private void resetCaretBlink() {
        blinkTick = 0;
        caretVisible = true;
    }

    private void tickCaretBlink() {
        blinkTick++;
        if (blinkTick >= BLINK_INTERVAL) {
            blinkTick = 0;
            caretVisible = !caretVisible;
            redraw();
        }
    }

    // ── Cursor position (offsets align with String.split("\n", -1)) ───────────

    private String[] linesSnapshot() {
        return text.toString().split("\n", -1);
    }

    /** Sets {@link #goalColumn} from {@link #cursorPos} (call after horizontal moves & typing). */
    private void syncGoalColumnFromCursor() {
        String[] lines = linesSnapshot();
        int counted = 0;
        for (int i = 0; i < lines.length; i++) {
            int len = lines[i].length();
            if (cursorPos <= counted + len) {
                goalColumn = cursorPos - counted;
                return;
            }
            counted += len + 1;
        }
        int last = lines.length - 1;
        goalColumn = lines[last].length();
    }

    private int offsetForLineColumn(int lineIdx, int col) {
        String[] lines = linesSnapshot();
        int p = 0;
        for (int i = 0; i < lineIdx; i++) {
            p += lines[i].length() + 1;
        }
        return p + Math.min(col, lines[lineIdx].length());
    }

    private boolean moveLeft() {
        if (cursorPos <= 0) {
            return false;
        }
        cursorPos--;
        syncGoalColumnFromCursor();
        redraw();
        return true;
    }

    private boolean moveRight() {
        if (cursorPos >= text.length()) {
            return false;
        }
        cursorPos++;
        syncGoalColumnFromCursor();
        redraw();
        return true;
    }

    private boolean moveUp() {
        String[] lines = linesSnapshot();
        int counted = 0;
        for (int i = 0; i < lines.length; i++) {
            int len = lines[i].length();
            if (cursorPos <= counted + len) {
                if (i == 0) {
                    return false;
                }
                int col = Math.min(goalColumn, lines[i - 1].length());
                cursorPos = offsetForLineColumn(i - 1, col);
                redraw();
                return true;
            }
            counted += len + 1;
        }
        return false;
    }

    private boolean moveDown() {
        String[] lines = linesSnapshot();
        int counted = 0;
        for (int i = 0; i < lines.length; i++) {
            int len = lines[i].length();
            if (cursorPos <= counted + len) {
                if (i >= lines.length - 1) {
                    return false;
                }
                int col = Math.min(goalColumn, lines[i + 1].length());
                cursorPos = offsetForLineColumn(i + 1, col);
                redraw();
                return true;
            }
            counted += len + 1;
        }
        return false;
    }

    private boolean moveLineStart() {
        String[] lines = linesSnapshot();
        int counted = 0;
        for (int i = 0; i < lines.length; i++) {
            int len = lines[i].length();
            if (cursorPos <= counted + len) {
                cursorPos = counted;
                syncGoalColumnFromCursor();
                redraw();
                return true;
            }
            counted += len + 1;
        }
        return false;
    }

    private boolean moveLineEnd() {
        String[] lines = linesSnapshot();
        int counted = 0;
        for (int i = 0; i < lines.length; i++) {
            int len = lines[i].length();
            if (cursorPos <= counted + len) {
                cursorPos = counted + len;
                syncGoalColumnFromCursor();
                redraw();
                return true;
            }
            counted += len + 1;
        }
        return false;
    }

    /** Draws one line using DSL spans from {@link CodeSyntaxHighlighter}. */
    private void drawHighlightedLine(GreenfootImage img, String line, int baseY) {
        List<CodeSyntaxHighlighter.Span> spans = CodeSyntaxHighlighter.spansForLine(line);
        int x = PAD_LEFT;
        for (CodeSyntaxHighlighter.Span sp : spans) {
            String chunk = line.substring(sp.start, sp.end);
            img.setColor(CodeSyntaxHighlighter.colorFor(sp.style));
            img.drawString(chunk, x, baseY);
            x += chunk.length() * CHAR_W;
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void redraw() {
        GreenfootImage img = new GreenfootImage(W, H);

        // Background
        img.setColor(new Color(22, 22, 32));
        img.fill();

        // Border
        img.setColor(new Color(70, 70, 160));
        img.drawRect(0, 0, W - 1, H - 1);

        // Title bar
        img.setColor(new Color(45, 45, 85));
        img.fillRect(0, 0, W, TITLE_H);
        img.setColor(new Color(160, 160, 240));
        img.setFont(new Font((int) Math.round(11 * GameScreenLayout.UI_SCALE)));
        img.drawString("  CODE EDITOR", 2, TITLE_H - (int) Math.round(5 * GameScreenLayout.UI_SCALE));

        // ── Split text into lines ─────────────────────────────────────────────
        String content  = text.toString();
        String[] lines  = content.split("\n", -1);

        // Resolve cursor → (line, col) in O(n)
        int cursorLine = 0;
        int cursorCol  = 0;
        int counted    = 0;
        for (int i = 0; i < lines.length; i++) {
            int len = lines[i].length();
            if (cursorPos <= counted + len) {
                cursorLine = i;
                cursorCol  = cursorPos - counted;
                break;
            }
            counted += len + 1;  // +1 for the '\n' that split consumed
        }

        // ── Draw lines (syntax-coloured via CodeSyntaxHighlighter + Lexer keywords) ──
        img.setFont(new Font("Monospaced", false, false, FONT_SIZE));

        for (int i = 0; i < lines.length; i++) {
            int baseY = TITLE_H + PAD_TOP + (i + 1) * LINE_H;
            if (baseY > H - 4) break;  // out of visible area

            // Highlight the line the cursor is on
            if (i == cursorLine) {
                img.setColor(new Color(38, 38, 60));
                img.fillRect(1, baseY - LINE_H + 3, W - 2, LINE_H);
            }

            // Line number
            img.setColor(new Color(90, 90, 110));
            img.drawString(String.valueOf(i + 1), 2, baseY);

            drawHighlightedLine(img, lines[i], baseY);

            if (i == cursorLine && caretVisible) {
                int cx = PAD_LEFT + cursorCol * CHAR_W;
                img.setColor(new Color(200, 200, 255));
                img.fillRect(cx, baseY - LINE_H + 3, 2, LINE_H - 2);
            }
        }

        setImage(img);
    }
}
