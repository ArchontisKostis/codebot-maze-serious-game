import greenfoot.*;

/**
 * Load Custom screen: shown after the player chooses Load Custom and before any level
 * loads. The player supplies a level as raw {@code .lvl} text or a base64 share-code —
 * via the PASTE button (OS clipboard) and/or by editing the {@link LevelInputField}
 * directly — then LOAD. On success the custom level launches in Free Play
 * (completion-only) mode; on failure a readable error is shown and the screen stays put.
 */
public class LoadCustomWorld extends World {

    private static final int W = GameScreenLayout.WORLD_WIDTH;
    private static final int H = GameScreenLayout.WORLD_HEIGHT;

    private static final int FIELD_W = GameScreenLayout.scale(620);
    private static final int FIELD_H = GameScreenLayout.scale(380);
    private static final int FIELD_TOP = GameScreenLayout.scale(110);

    private static final String EDITOR_PROMPT = "Want to make your own level?";
    private static final String EDITOR_URL = "Use the level editor at: https://codebot.archontis.gr/lvl-editor";

    private final LevelInputField input;
    private String status = "Paste a .lvl document or share-code (or type one), then press LOAD.";

    public LoadCustomWorld() {
        super(W, H, 1);

        input = new LevelInputField(FIELD_W, FIELD_H,
            "Paste or type a .lvl document or base64 share-code here…");
        addObject(input, W / 2, FIELD_TOP + FIELD_H / 2);

        layoutButtons();

        drawBackground();
    }

    /** Lays out PASTE / CLEAR / LOAD / BACK as a centered row of artwork buttons beneath the field. */
    private void layoutButtons() {
        int btnY = H - GameScreenLayout.scale(64);
        int btnW = GameScreenLayout.scale(140);
        int btnH = GameScreenLayout.scale(47);
        int gap = GameScreenLayout.scale(16);
        int totalW = btnW * 4 + gap * 3;
        int leftCenter = W / 2 - totalW / 2 + btnW / 2;
        int step = btnW + gap;

        addImageButton("ui/back-btn.png", "BACK", () -> Greenfoot.setWorld(new FreePlayWorld()),
            leftCenter, btnY, btnW, btnH);
        addImageButton("ui/clear-btn.png", "CLEAR", this::clearInput, leftCenter + step, btnY, btnW, btnH);
        addImageButton("ui/paste-btn.png", "PASTE", this::pasteFromClipboard, leftCenter + step * 2, btnY, btnW, btnH);
        addImageButton("ui/load-custom-btn.png", "LOAD", this::loadLevel, leftCenter + step * 3, btnY, btnW, btnH);
    }

    /** Adds an artwork button, falling back to a text button if the image is missing. */
    private void addImageButton(String imagePath, String label, Runnable action, int x, int y, int width, int height) {
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

    private void pasteFromClipboard() {
        String text = ClipboardAccess.read();
        if (text == null || text.isEmpty()) {
            setStatus("Clipboard is empty or unavailable — type or edit the level instead.");
            return;
        }
        input.setText(text);
        setStatus("Pasted " + text.length() + " characters. Edit if needed, then press LOAD.");
    }

    private void clearInput() {
        input.clear();
        setStatus("Cleared. Paste or type a level, then press LOAD.");
    }

    private void loadLevel() {
        LevelLoadResult result = LevelDocumentParser.load(input.getText());
        if (!result.isOk()) {
            setStatus(result.getError());
            return;
        }
        // The mode decides scoring: a custom document is always completion-scored.
        LevelDefinition def = result.getDefinition().withCompletionScoring();
        Greenfoot.setWorld(new LoadingWorld(() -> new SimulationWorld(new CustomLevel(def), true)));
    }

    private void setStatus(String message) {
        status = message;
        drawBackground();
    }

    private void drawBackground() {
        GreenfootImage bg = new GreenfootImage("ui/generic-bg.png");
        bg.scale(W, H);

        GreenfootImage title = new GreenfootImage("LOAD CUSTOM LEVEL",
            GameScreenLayout.scale(26), new Color(44, 220, 215), new Color(0, 0, 0, 0));
        bg.drawImage(title, (W - title.getWidth()) / 2, GameScreenLayout.scale(38));

        GreenfootImage st = new GreenfootImage(status,
            GameScreenLayout.scale(14), new Color(240, 200, 120), new Color(0, 0, 0, 0));
        bg.drawImage(st, (W - st.getWidth()) / 2, GameScreenLayout.scale(76));

        // Pointer to the web level editor, in the gap between the field and the button row.
        Color transparent = new Color(0, 0, 0, 0);
        int linkY = FIELD_TOP + FIELD_H + GameScreenLayout.scale(12);
        GreenfootImage prompt = new GreenfootImage(EDITOR_PROMPT,
            GameScreenLayout.scale(13), new Color(180, 190, 200), transparent);
        bg.drawImage(prompt, (W - prompt.getWidth()) / 2, linkY);

        GreenfootImage url = new GreenfootImage(EDITOR_URL,
            GameScreenLayout.scale(14), new Color(44, 220, 215), transparent);
        bg.drawImage(url, (W - url.getWidth()) / 2, linkY + prompt.getHeight() + GameScreenLayout.scale(4));

        setBackground(bg);
    }
}
