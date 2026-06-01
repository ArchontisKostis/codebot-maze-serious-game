import greenfoot.*;
import java.util.List;
import java.util.function.Predicate;

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
    private static final int PAD_LEFT   = (int) Math.round(32 * GameScreenLayout.UI_SCALE);
    private static final int PAD_TOP    = (int) Math.round(6 * GameScreenLayout.UI_SCALE);
    private static final int CHAR_W     = (int) Math.round(7 * GameScreenLayout.UI_SCALE);
    private static final int LINE_NUMBER_X = GameScreenLayout.scale(14);
    private static final Color ACTIVE_LINE_BG = new Color(255, 255, 255, 20);
    private static final int CONTENT_BOTTOM_Y = H - GameScreenLayout.scale(4);
    private static final int SCROLLBAR_W = GameScreenLayout.scale(6);
    private static final int SCROLLBAR_RIGHT_PAD = GameScreenLayout.scale(4);
    private static final int SCROLLBAR_TOP = TITLE_H + GameScreenLayout.scale(6);
    private static final int SCROLLBAR_BOTTOM = H - GameScreenLayout.scale(9);
    private static final int SCROLLBAR_MIN_THUMB_H = GameScreenLayout.scale(14);
    private static final int STEP_BTN_W = GameScreenLayout.scale(48);
    private static final int STEP_BTN_H = GameScreenLayout.scale(15);
    private static final int STEP_BTN_X = W - STEP_BTN_W - GameScreenLayout.scale(8);
    private static final int STEP_BTN_Y = GameScreenLayout.scale(6);
    private static final GreenfootImage EDITOR_BACKGROUND = loadEditorBackground();
    private static final GreenfootImage STEP_BUTTON_ART = loadStepButtonArt();

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
    private int executingLine = -1;
    private int firstVisibleLine = 0;
    private boolean scrollbarDragging = false;
    private int scrollbarDragOffsetY = 0;
    private final Runnable stepAction;
    private final Predicate<String> enterInterceptor;

    // ── Constructor ───────────────────────────────────────────────────────────

    public CodeEditor() {
        this(null, null);
    }

    public CodeEditor(Runnable stepAction) {
        this(stepAction, null);
    }

    public CodeEditor(Runnable stepAction, Predicate<String> enterInterceptor) {
        this.stepAction = stepAction;
        this.enterInterceptor = enterInterceptor;
        redraw();
    }

    private static GreenfootImage loadEditorBackground() {
        try {
            GreenfootImage bg = new GreenfootImage("ui/code-editor-bg.png");
            if (bg.getWidth() != W || bg.getHeight() != H) {
                bg.scale(W, H);
            }
            return bg;
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private static GreenfootImage loadStepButtonArt() {
        try {
            GreenfootImage art = new GreenfootImage("ui/step-btn.png");
            if (art.getWidth() != STEP_BTN_W || art.getHeight() != STEP_BTN_H) {
                art.scale(STEP_BTN_W, STEP_BTN_H);
            }
            return art;
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    // ── Greenfoot lifecycle ───────────────────────────────────────────────────

    @Override
    public void act() {
        handleMouse();

        String key = Greenfoot.getKey();
        if (key != null && handleKey(key)) {
            resetCaretBlink();
        }
        tickCaretBlink();
    }

    private void handleMouse() {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse == null) {
            return;
        }

        int localX = mouse.getX() - (getX() - W / 2);
        int localY = mouse.getY() - (getY() - H / 2);

        if (Greenfoot.mouseClicked(this) && stepAction != null && insideStepButton(localX, localY)) {
            Sfx.buttonClick();
            stepAction.run();
            return;
        }

        ScrollbarMetrics scroll = computeScrollbarMetrics(linesSnapshot().length);
        if (!scroll.show) {
            if (scrollbarDragging) {
                scrollbarDragging = false;
            }
            return;
        }

        if (Greenfoot.mousePressed(this)) {
            if (insideScrollbarThumb(localX, localY, scroll)) {
                scrollbarDragging = true;
                scrollbarDragOffsetY = localY - scroll.thumbY;
                return;
            }
            if (insideScrollbarTrack(localX, localY, scroll)) {
                if (localY < scroll.thumbY) {
                    pageScroll(-1, scroll.visibleLineCount, scroll.totalLines);
                } else if (localY > scroll.thumbY + scroll.thumbH) {
                    pageScroll(1, scroll.visibleLineCount, scroll.totalLines);
                }
            }
        }

        if (scrollbarDragging && Greenfoot.mouseDragged(this)) {
            updateScrollFromDrag(localY, scroll);
        }

        if (scrollbarDragging && Greenfoot.mouseDragEnded(this)) {
            scrollbarDragging = false;
        }
    }

    private boolean insideStepButton(int x, int y) {
        return x >= STEP_BTN_X && x <= STEP_BTN_X + STEP_BTN_W
            && y >= STEP_BTN_Y && y <= STEP_BTN_Y + STEP_BTN_H;
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
        executingLine = -1;
        firstVisibleLine = 0;
        scrollbarDragging = false;
        resetCaretBlink();
        redraw();
    }

    /** Shows a small marker beside the 1-based source line currently being executed. */
    public void setExecutingLine(int lineNumber) {
        if (executingLine == lineNumber) {
            return;
        }
        executingLine = lineNumber;
        if (executingLine > 0) {
            ensureLineVisible(executingLine - 1, linesSnapshot().length, visibleLineCapacity());
        }
        redraw();
    }

    /** Removes the execution marker. */
    public void clearExecutingLine() {
        setExecutingLine(-1);
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
                    ensureCursorVisible();
                    redraw();
                    return true;
                }
                return false;

            case "enter":
                if (enterInterceptor != null && enterInterceptor.test(text.toString())) {
                    return true;
                }
                text.insert(cursorPos, '\n');
                cursorPos++;
                syncGoalColumnFromCursor();
                ensureCursorVisible();
                redraw();
                return true;

            case "space":
                text.insert(cursorPos, ' ');
                cursorPos++;
                syncGoalColumnFromCursor();
                ensureCursorVisible();
                redraw();
                return true;

            case "tab":
                text.insert(cursorPos, "  ");   // two-space indent
                cursorPos += 2;
                syncGoalColumnFromCursor();
                ensureCursorVisible();
                redraw();
                return true;

            case "delete":
            case "del":
                if (cursorPos < text.length()) {
                    text.deleteCharAt(cursorPos);
                    ensureCursorVisible();
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
        ensureCursorVisible();
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
        ensureCursorVisible();
        redraw();
        return true;
    }

    private boolean moveRight() {
        if (cursorPos >= text.length()) {
            return false;
        }
        cursorPos++;
        syncGoalColumnFromCursor();
        ensureCursorVisible();
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
                ensureCursorVisible();
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
                ensureCursorVisible();
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
                ensureCursorVisible();
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
                ensureCursorVisible();
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

        img.setColor(Color.BLACK);
        img.fill();

        if (EDITOR_BACKGROUND != null) {
            img.drawImage(EDITOR_BACKGROUND, 0, 0);
        }

        drawStepButton(img);

        // ── Split text into lines ─────────────────────────────────────────────
        String content  = text.toString();
        String[] lines  = content.split("\n", -1);
        int totalLines = lines.length;
        int visibleLineCount = visibleLineCapacity();
        clampFirstVisibleLine(totalLines, visibleLineCount);
        ScrollbarMetrics scroll = computeScrollbarMetrics(totalLines);

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

        int startLine = firstVisibleLine;
        int endLine = Math.min(totalLines, startLine + visibleLineCount);
        int contentRight = scroll.show ? scroll.trackX - GameScreenLayout.scale(4) : W - 2;

        for (int i = startLine; i < endLine; i++) {
            int row = i - startLine;
            int baseY = TITLE_H + PAD_TOP + (row + 1) * LINE_H;
            if (baseY > CONTENT_BOTTOM_Y) break;

            // Highlight the line the cursor is on
            if (i == cursorLine) {
                img.setColor(ACTIVE_LINE_BG);
                img.fillRect(1, baseY - LINE_H + 3, contentRight - 1, LINE_H);
            }

            // Line number
            img.setColor(new Color(90, 90, 110));
            img.drawString(String.valueOf(i + 1), LINE_NUMBER_X, baseY);

            if (i + 1 == executingLine) {
                int dotSize = Math.max(5, GameScreenLayout.scale(7));
                int dotX = Math.max(1, PAD_LEFT - GameScreenLayout.scale(12));
                int dotY = baseY - LINE_H / 2;
                img.setColor(new Color(80, 230, 150));
                img.fillOval(dotX, dotY, dotSize, dotSize);
            }

            drawHighlightedLine(img, lines[i], baseY);

            if (i == cursorLine && caretVisible) {
                int cx = PAD_LEFT + cursorCol * CHAR_W;
                img.setColor(new Color(200, 200, 255));
                img.fillRect(cx, baseY - LINE_H + 3, 2, LINE_H - 2);
            }
        }

        drawScrollbar(img, scroll);

        setImage(img);
    }

    private int visibleLineCapacity() {
        int firstBaselineY = TITLE_H + PAD_TOP + LINE_H;
        int drawableHeight = CONTENT_BOTTOM_Y - firstBaselineY;
        return Math.max(1, drawableHeight / LINE_H + 1);
    }

    private void clampFirstVisibleLine(int totalLines, int visibleLineCount) {
        int maxFirst = Math.max(0, totalLines - visibleLineCount);
        if (firstVisibleLine < 0) {
            firstVisibleLine = 0;
        } else if (firstVisibleLine > maxFirst) {
            firstVisibleLine = maxFirst;
        }
    }

    private void ensureCursorVisible() {
        String[] lines = linesSnapshot();
        int cursorLine = lineIndexForCursor(lines);
        int visibleLineCount = visibleLineCapacity();
        ensureLineVisible(cursorLine, lines.length, visibleLineCount);
    }

    private void ensureLineVisible(int lineIdx, int totalLines, int visibleLineCount) {
        clampFirstVisibleLine(totalLines, visibleLineCount);
        if (lineIdx < firstVisibleLine) {
            firstVisibleLine = lineIdx;
            clampFirstVisibleLine(totalLines, visibleLineCount);
            return;
        }
        int lastVisible = firstVisibleLine + visibleLineCount - 1;
        if (lineIdx > lastVisible) {
            firstVisibleLine = lineIdx - visibleLineCount + 1;
            clampFirstVisibleLine(totalLines, visibleLineCount);
        }
    }

    private int lineIndexForCursor(String[] lines) {
        int counted = 0;
        for (int i = 0; i < lines.length; i++) {
            int len = lines[i].length();
            if (cursorPos <= counted + len) {
                return i;
            }
            counted += len + 1;
        }
        return Math.max(0, lines.length - 1);
    }

    private void pageScroll(int direction, int visibleLineCount, int totalLines) {
        firstVisibleLine += direction * Math.max(1, visibleLineCount - 1);
        clampFirstVisibleLine(totalLines, visibleLineCount);
        redraw();
    }

    private void updateScrollFromDrag(int localY, ScrollbarMetrics scroll) {
        if (scroll.trackTravel <= 0 || scroll.maxFirstLine <= 0) {
            return;
        }
        int unclampedThumbY = localY - scrollbarDragOffsetY;
        int clampedThumbY = Math.max(scroll.trackY, Math.min(scroll.trackY + scroll.trackTravel, unclampedThumbY));
        int relative = clampedThumbY - scroll.trackY;
        firstVisibleLine = (int) Math.round((relative * (double) scroll.maxFirstLine) / scroll.trackTravel);
        clampFirstVisibleLine(scroll.totalLines, scroll.visibleLineCount);
        redraw();
    }

    private boolean insideScrollbarTrack(int x, int y, ScrollbarMetrics scroll) {
        return scroll.show
            && x >= scroll.trackX && x <= scroll.trackX + scroll.trackW
            && y >= scroll.trackY && y <= scroll.trackY + scroll.trackH;
    }

    private boolean insideScrollbarThumb(int x, int y, ScrollbarMetrics scroll) {
        return scroll.show
            && x >= scroll.trackX && x <= scroll.trackX + scroll.trackW
            && y >= scroll.thumbY && y <= scroll.thumbY + scroll.thumbH;
    }

    private ScrollbarMetrics computeScrollbarMetrics(int totalLines) {
        int visibleLineCount = visibleLineCapacity();
        int maxFirstLine = Math.max(0, totalLines - visibleLineCount);
        boolean show = totalLines > visibleLineCount;
        int trackX = W - SCROLLBAR_RIGHT_PAD - SCROLLBAR_W;
        int trackY = SCROLLBAR_TOP;
        int trackH = Math.max(1, SCROLLBAR_BOTTOM - SCROLLBAR_TOP);
        int thumbH = trackH;
        if (show) {
            int scaledThumb = (int) Math.round((visibleLineCount * (double) trackH) / totalLines);
            thumbH = Math.max(SCROLLBAR_MIN_THUMB_H, Math.min(trackH, scaledThumb));
        }
        int trackTravel = Math.max(0, trackH - thumbH);
        int thumbY = trackY;
        if (show && maxFirstLine > 0 && trackTravel > 0) {
            thumbY += (int) Math.round((firstVisibleLine * (double) trackTravel) / maxFirstLine);
        }
        return new ScrollbarMetrics(show, totalLines, visibleLineCount, maxFirstLine,
            trackX, trackY, SCROLLBAR_W, trackH, thumbY, thumbH, trackTravel);
    }

    private void drawScrollbar(GreenfootImage img, ScrollbarMetrics scroll) {
        if (!scroll.show) {
            return;
        }
        img.setColor(new Color(20, 30, 50, 170));
        img.fillRect(scroll.trackX, scroll.trackY, scroll.trackW, scroll.trackH);
        img.setColor(new Color(55, 88, 145, 200));
        img.drawRect(scroll.trackX, scroll.trackY, scroll.trackW - 1, scroll.trackH - 1);

        img.setColor(new Color(145, 185, 245, 220));
        img.fillRect(scroll.trackX + 1, scroll.thumbY, Math.max(1, scroll.trackW - 2), scroll.thumbH);
        img.setColor(new Color(205, 225, 255, 230));
        img.drawRect(scroll.trackX + 1, scroll.thumbY, Math.max(1, scroll.trackW - 2), Math.max(1, scroll.thumbH - 1));
    }

    private static final class ScrollbarMetrics {
        final boolean show;
        final int totalLines;
        final int visibleLineCount;
        final int maxFirstLine;
        final int trackX;
        final int trackY;
        final int trackW;
        final int trackH;
        final int thumbY;
        final int thumbH;
        final int trackTravel;

        ScrollbarMetrics(boolean show, int totalLines, int visibleLineCount, int maxFirstLine,
            int trackX, int trackY, int trackW, int trackH, int thumbY, int thumbH, int trackTravel) {
            this.show = show;
            this.totalLines = totalLines;
            this.visibleLineCount = visibleLineCount;
            this.maxFirstLine = maxFirstLine;
            this.trackX = trackX;
            this.trackY = trackY;
            this.trackW = trackW;
            this.trackH = trackH;
            this.thumbY = thumbY;
            this.thumbH = thumbH;
            this.trackTravel = trackTravel;
        }
    }

    private void drawStepButton(GreenfootImage img) {
        if (STEP_BUTTON_ART != null) {
            img.drawImage(STEP_BUTTON_ART, STEP_BTN_X, STEP_BTN_Y);
            return;
        }

        img.setColor(new Color(68, 82, 130));
        img.fillRect(STEP_BTN_X, STEP_BTN_Y, STEP_BTN_W, STEP_BTN_H);
        img.setColor(new Color(140, 165, 235));
        img.drawRect(STEP_BTN_X, STEP_BTN_Y, STEP_BTN_W, STEP_BTN_H);
        img.setColor(new Color(225, 235, 255));
        img.setFont(new Font("SansSerif", true, false, GameScreenLayout.scale(9)));
        img.drawString("STEP", STEP_BTN_X + GameScreenLayout.scale(9), STEP_BTN_Y + GameScreenLayout.scale(11));
    }
}
