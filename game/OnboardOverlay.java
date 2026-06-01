import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class OnboardOverlay extends Actor {

    // ── Palette ──────────────────────────────────────────────────────────────

    // Spotlight dimming + glow are onboarding-specific accents; the card chrome
    // and buttons come from the shared UiTheme palette.
    private static final Color DARK        = new Color(0,   0,   0,   185);
    private static final Color GLOW_1      = new Color(100, 160, 255, 210);
    private static final Color GLOW_2      = new Color(80,  130, 220, 140);
    private static final Color CARD_BG     = UiTheme.CARD_BG;
    private static final Color CARD_BORDER = UiTheme.BORDER;
    private static final Color TITLE_COL   = UiTheme.TITLE;
    private static final Color TEXT_COL    = UiTheme.BODY;
    private static final Color BTN_NEXT_BG = UiTheme.BTN_PRIMARY;
    private static final Color BTN_NEXT_BD = UiTheme.BTN_PRIMARY_BORDER;
    private static final Color BTN_BACK_BG = UiTheme.BTN_GHOST;
    private static final Color BTN_BACK_BD = UiTheme.BTN_GHOST_BORDER;
    private static final Color DOT_ON      = new Color(100, 160, 255);
    private static final Color DOT_OFF     = new Color(50,  65,  85);

    // ── Layout ───────────────────────────────────────────────────────────────

    private static final int W = GameScreenLayout.WORLD_WIDTH;
    private static final int H = GameScreenLayout.WORLD_HEIGHT;

    // ── State ────────────────────────────────────────────────────────────────

    private final OnboardFlow flow;
    private final SimulationWorld world;
    private int stepIndex = 0;

    // Button hit-boxes in world coordinates (set each redraw)
    private int backX, backY, backW, backH;
    private int nextX, nextY, nextW, nextH;

    // ── Construction ─────────────────────────────────────────────────────────

    public OnboardOverlay(OnboardFlow flow, SimulationWorld world) {
        this.flow  = flow;
        this.world = world;
        world.setIntroActive(true);
        redraw();
    }

    // ── Act ──────────────────────────────────────────────────────────────────

    @Override
    public void act() {
        if (!Greenfoot.mouseClicked(this)) return;
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse == null) return;
        int mx = mouse.getX();
        int my = mouse.getY();

        if (stepIndex > 0 && inside(mx, my, backX, backY, backW, backH)) {
            Sfx.buttonClick();
            stepIndex--;
            redraw();
        } else if (inside(mx, my, nextX, nextY, nextW, nextH)) {
            Sfx.buttonClick();
            if (stepIndex < flow.size() - 1) {
                stepIndex++;
                redraw();
            } else {
                close();
            }
        }
    }

    // ── Close ────────────────────────────────────────────────────────────────

    public void close() {
        World w = getWorld();
        if (w != null) w.removeObject(this);
        world.setIntroActive(false);
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    private void redraw() {
        OnboardStep step   = flow.get(stepIndex);
        GreenfootImage img = new GreenfootImage(W, H);

        int[] spot = step.spotlight.toRect();
        if (spot == null) {
            img.setColor(DARK);
            img.fill();
        } else {
            drawSurround(img, spot);
            drawGlow(img, spot);
        }

        drawCard(img, step, spot);
        setImage(img);
    }

    /** Four dark rectangles surrounding the spotlight, leaving it transparent. */
    private void drawSurround(GreenfootImage img, int[] r) {
        int sx = r[0], sy = r[1], sw = r[2], sh = r[3];
        img.setColor(DARK);
        img.fillRect(0,       0,       W,            sy);          // top
        img.fillRect(0,       sy + sh, W,            H - sy - sh); // bottom
        img.fillRect(0,       sy,      sx,           sh);          // left
        img.fillRect(sx + sw, sy,      W - sx - sw,  sh);          // right
    }

    private void drawGlow(GreenfootImage img, int[] r) {
        int sx = r[0], sy = r[1], sw = r[2], sh = r[3];
        img.setColor(GLOW_1);
        img.drawRect(sx - 2, sy - 2, sw + 3, sh + 3);
        img.setColor(GLOW_2);
        img.drawRect(sx - 3, sy - 3, sw + 5, sh + 5);
    }

    // ── Card ─────────────────────────────────────────────────────────────────

    private void drawCard(GreenfootImage img, OnboardStep step, int[] spot) {
        int[] c = cardRect(step.cardSide);
        int cx = c[0], cy = c[1], cw = c[2], ch = c[3];

        // Background and border
        img.setColor(CARD_BG);
        img.fillRect(cx, cy, cw, ch);
        img.setColor(CARD_BORDER);
        img.drawRect(cx,     cy,     cw,     ch);
        img.drawRect(cx + 1, cy + 1, cw - 2, ch - 2);

        int pad  = GameScreenLayout.scale(12);
        int conX = cx + pad;
        int conW = cw - pad * 2;

        // Title
        int titleSize = GameScreenLayout.scale(13);
        img.setFont(new Font("SansSerif", true, false, titleSize));
        img.setColor(TITLE_COL);
        int titleY = cy + GameScreenLayout.scale(20);
        drawWrapped(img, step.title, conX, titleY, conW, titleSize, GameScreenLayout.scale(16), 2);

        // Body lines — each source line wrapped to the card width, then drawn.
        // Wrap only splits lines that overflow; short and code-style lines stay intact.
        int bodySize = GameScreenLayout.scale(11);
        img.setFont(new Font("SansSerif", false, false, bodySize));
        img.setColor(TEXT_COL);
        int lineH = GameScreenLayout.scale(16);
        int bodyY = cy + GameScreenLayout.scale(48);
        int maxBody = (ch - GameScreenLayout.scale(110)) / lineH;
        int bodyChars = Math.max(8, conW / Math.max(1, (int)(bodySize * 0.60)));
        List<String> bodyLines = new ArrayList<>();
        for (String line : step.lines) {
            if (line.isEmpty()) {
                bodyLines.add("");                 // preserve intentional blank lines
            } else {
                bodyLines.addAll(wrap(line, bodyChars));
            }
        }
        for (int i = 0; i < bodyLines.size() && i < maxBody; i++) {
            img.drawString(bodyLines.get(i), conX, bodyY + i * lineH);
        }

        // Step dots
        int dotsY = cy + ch - GameScreenLayout.scale(40);
        drawDots(img, cx, cw, dotsY);

        // Buttons (also stores hit-boxes)
        drawButtons(img, cx, cy, cw, ch);
    }

    private void drawDots(GreenfootImage img, int cx, int cw, int y) {
        int total   = flow.size();
        int dotSize = GameScreenLayout.scale(7);
        int dotGap  = GameScreenLayout.scale(9);
        int totalW  = total * dotSize + (total - 1) * dotGap;
        int startX  = cx + (cw - totalW) / 2;
        for (int i = 0; i < total; i++) {
            img.setColor(i == stepIndex ? DOT_ON : DOT_OFF);
            img.fillOval(startX + i * (dotSize + dotGap), y, dotSize, dotSize);
        }
    }

    private void drawButtons(GreenfootImage img, int cx, int cy, int cw, int ch) {
        int btnH  = GameScreenLayout.scale(28);
        int btnY  = cy + ch - GameScreenLayout.scale(14) - btnH;
        int pad   = GameScreenLayout.scale(12);
        boolean isLast = (stepIndex == flow.size() - 1);

        // Next / Start
        nextW = isLast ? GameScreenLayout.scale(90) : GameScreenLayout.scale(76);
        nextH = btnH;
        nextX = cx + cw - pad - nextW;
        nextY = btnY;
        drawBtn(img, nextX, nextY, nextW, nextH, isLast ? "Start" : "Next", BTN_NEXT_BG, BTN_NEXT_BD);

        // Back (hidden on first step)
        if (stepIndex > 0) {
            backW = GameScreenLayout.scale(76);
            backH = btnH;
            backX = cx + pad;
            backY = btnY;
            drawBtn(img, backX, backY, backW, backH, "Back", BTN_BACK_BG, BTN_BACK_BD);
        } else {
            backX = -1; backY = -1; backW = 0; backH = 0;
        }
    }

    private void drawBtn(GreenfootImage img, int x, int y, int w, int h,
            String label, Color bg, Color border) {
        img.setColor(bg);
        img.fillRect(x, y, w, h);
        img.setColor(border);
        img.drawRect(x, y, w - 1, h - 1);

        int fs = GameScreenLayout.scale(12);
        img.setFont(new Font("SansSerif", true, false, fs));
        img.setColor(Color.WHITE);
        int textW = label.length() * (int)(fs * 0.60);
        int textX = x + Math.max(GameScreenLayout.scale(6), (w - textW) / 2);
        int textY = y + h / 2 + fs / 2 - GameScreenLayout.scale(1);
        img.drawString(label, textX, textY);
    }

    // ── Card geometry ─────────────────────────────────────────────────────────

    /**
     * Returns {x, y, w, h} for the tooltip card depending on which dark
     * area it should occupy relative to the spotlight.
     *
     *   RIGHT  → CODE_EDITOR column  (720..960, card in dark area when GAME_AREA is lit)
     *   LEFT   → left game area      (0..720,   card in dark area when editor/controls lit)
     *   CENTER → centred on canvas   (for NONE spotlight / welcome step)
     */
    private int[] cardRect(OnboardStep.CardSide side) {
        int pad = GameScreenLayout.scale(12);
        switch (side) {
            case RIGHT: {
                int areaX = GameScreenLayout.SCRIPT_AREA_X;
                int cw    = GameScreenLayout.SCRIPT_AREA_W - pad * 2;
                int ch    = GameScreenLayout.scale(330);
                int cx    = areaX + pad;
                int cy    = GameScreenLayout.HUD_STRIP_H + GameScreenLayout.scale(30);
                return new int[]{cx, cy, cw, ch};
            }
            case LEFT: {
                int cw = GameScreenLayout.scale(530);
                int ch = GameScreenLayout.scale(260);
                int cx = pad;
                int cy = GameScreenLayout.HUD_STRIP_H + GameScreenLayout.scale(30);
                return new int[]{cx, cy, cw, ch};
            }
            case CENTER:
            default: {
                int cw = GameScreenLayout.scale(520);
                int ch = GameScreenLayout.scale(280);
                int cx = (W - cw) / 2;
                int cy = (H - ch) / 2;
                return new int[]{cx, cy, cw, ch};
            }
        }
    }

    // ── Text helpers ─────────────────────────────────────────────────────────

    private void drawWrapped(GreenfootImage img, String text, int x, int y,
            int maxW, int fontSize, int lineH, int maxLines) {
        int charsPerLine = Math.max(8, maxW / Math.max(1, (int)(fontSize * 0.60)));
        List<String> lines = wrap(text, charsPerLine);
        for (int i = 0; i < lines.size() && i < maxLines; i++) {
            img.drawString(lines.get(i), x, y + i * lineH);
        }
    }

    private List<String> wrap(String text, int maxChars) {
        List<String> result = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            // Hard-break a single token too long to ever fit (e.g. a URL) so it
            // never overflows the card horizontally.
            while (word.length() > maxChars) {
                if (line.length() > 0) {
                    result.add(line.toString());
                    line = new StringBuilder();
                }
                result.add(word.substring(0, maxChars));
                word = word.substring(maxChars);
            }
            if (line.length() == 0) {
                line.append(word);
            } else if (line.length() + 1 + word.length() <= maxChars) {
                line.append(' ').append(word);
            } else {
                result.add(line.toString());
                line = new StringBuilder(word);
            }
        }
        if (line.length() > 0) result.add(line.toString());
        return result;
    }

    private boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
