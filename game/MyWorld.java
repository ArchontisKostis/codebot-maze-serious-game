import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MyWorld
 *
 * Layout matches {@link GameScreenLayout} (scaled from the classic 800×600 design):
 *
 *   +------------------+--------+
 *   |                  |  CODE  |
 *   |    GAME AREA     | EDITOR |
 *   | (game W × game H)|(script|
 *   +------------------+--------+
 *   |   OUTPUT / TERMINAL       | CONTROLS |
 *   |    (game W × terminal H)  |          |
 *   +---------------------------+----------+
 *
 * Responsibilities:
 *   - Hosts CodeEditor in the script area
 *   - RUN button: lexes + parses editor text, hands Program to robot
 *   - RESET button: returns the robot to its home tile
 *   - Terminal log display (bottom left)
 *   - Tiled game area via {@link #installTileLevel(ParsedTileLevel)}
 */
public class MyWorld extends World {

    // ── Layout (delegates to GameScreenLayout — keep proportions in one place) ──

    public static final int GAME_AREA_MIN_X = 0;
    public static final int GAME_AREA_MAX_X = GameAreaConfig.GAME_AREA_WIDTH_PX;
    public static final int GAME_AREA_MIN_Y = GameScreenLayout.HUD_STRIP_H;
    public static final int GAME_AREA_MAX_Y = GameScreenLayout.HUD_STRIP_H + GameAreaConfig.GAME_AREA_HEIGHT_PX;

    public static final int SCRIPT_AREA_X   = GameScreenLayout.SCRIPT_AREA_X;
    public static final int SCRIPT_AREA_W   = GameScreenLayout.SCRIPT_AREA_W;
    public static final int SCRIPT_AREA_Y   = GameScreenLayout.SCRIPT_AREA_Y;
    public static final int SCRIPT_AREA_H   = GameScreenLayout.SCRIPT_AREA_H;

    public static final int TERMINAL_X      = GameScreenLayout.TERMINAL_X;
    public static final int TERMINAL_Y      = GameScreenLayout.TERMINAL_Y;
    public static final int TERMINAL_W      = GameScreenLayout.TERMINAL_W;
    public static final int TERMINAL_H      = GameScreenLayout.TERMINAL_H;

    public static final int CONTROLS_X      = GameScreenLayout.CONTROLS_X;
    public static final int CONTROLS_Y      = GameScreenLayout.CONTROLS_Y;
    public static final int CONTROLS_W      = GameScreenLayout.CONTROLS_W;
    public static final int CONTROLS_H      = GameScreenLayout.CONTROLS_H;

    /** Fits inside {@link GameScreenLayout#TERMINAL_H} with scaled fonts. */
    private static final int MAX_TERMINAL_LINES = 5;
    private static final int TERMINAL_LINE_H    = GameScreenLayout.scale(12);

    // ── State ─────────────────────────────────────────────────────────────────

    private RobotActor  robot;
    private CodeEditor  editor;
    private Level       level;
    private TileMap     tileMap;
    private final List<String> terminalLog = new ArrayList<>();
    private int attempts = 0;
    private int goalAdvanceCountdown = -1;
    private int     totalCoins     = 0;
    private int     coinsCollected = 0;
    private Program lastProgram    = null;
    private boolean introOverlayActive = false;
    private boolean programEndOverlayActive = false;

    private static final int PROGRAM_STOP_CARD_W = GameScreenLayout.scale(420);
    private static final int PROGRAM_STOP_CARD_H = GameScreenLayout.scale(210);
    private static final int PROGRAM_STOP_CARD_X = (GameScreenLayout.WORLD_WIDTH - PROGRAM_STOP_CARD_W) / 2;
    private static final int PROGRAM_STOP_CARD_Y = (GameScreenLayout.WORLD_HEIGHT - PROGRAM_STOP_CARD_H) / 2;
    private static final int PROGRAM_STOP_BUTTON_W = GameScreenLayout.scale(96);
    private static final int PROGRAM_STOP_BUTTON_H = GameScreenLayout.scale(34);
    private static final int PROGRAM_STOP_BUTTON_X = PROGRAM_STOP_CARD_X + (PROGRAM_STOP_CARD_W - PROGRAM_STOP_BUTTON_W) / 2;
    private static final int PROGRAM_STOP_BUTTON_Y = PROGRAM_STOP_CARD_Y + PROGRAM_STOP_CARD_H - GameScreenLayout.scale(58);

    // ── Constructor ───────────────────────────────────────────────────────────

    public MyWorld(Level level) {
        super(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT, 1);
        this.level = level;

        drawStaticUI();
        createActors();

        level.setup(this);
        redrawTerminal();
        redrawHUD();

        int levelNumber = LevelManager.getCurrentLevelNumber();
        OnboardFlow onboardFlow = OnboardRegistry.forLevel(levelNumber);
        if (onboardFlow != null) {
            addObject(new OnboardOverlay(onboardFlow, this),
                GameScreenLayout.WORLD_WIDTH / 2,
                GameScreenLayout.WORLD_HEIGHT / 2);
        }
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void drawStaticUI() {
        GreenfootImage bg = getBackground();

        // Full canvas base — do not paint the game rectangle here (tiles fill it in
        // {@link #installTileLevel}); a light-grey under-fill showed as false “empty rows”.
        bg.setColor(Color.BLACK);
        bg.fill();

        // Code editor strip (was default black / empty in the web player)
        bg.setColor(new Color(26, 26, 38));
        bg.fillRect(SCRIPT_AREA_X, SCRIPT_AREA_Y, SCRIPT_AREA_W, SCRIPT_AREA_H);

        // Terminal — slightly lighter than pure black so it reads as a panel, not “void”
        bg.setColor(new Color(36, 38, 46));
        bg.fillRect(TERMINAL_X, TERMINAL_Y, TERMINAL_W, TERMINAL_H);

        // Controls area — draw a dark panel background then overlay the controls
        // artwork slightly inset so it reads with padding.
        GreenfootImage controlsImg = null;
        try {
            controlsImg = new GreenfootImage("ui/controls-bg.png");
        } catch (IllegalArgumentException | NullPointerException e) {
            controlsImg = null;
        }

        // Panel base color as requested (#010412)
        bg.setColor(new Color(1, 4, 18));
        bg.fillRect(CONTROLS_X, CONTROLS_Y, CONTROLS_W, CONTROLS_H);

        if (controlsImg != null) {
            int imgW = (int)(CONTROLS_W * 0.90);
            int imgH = (int)(CONTROLS_H * 0.90);
            controlsImg.scale(imgW, imgH);
            int imgX = CONTROLS_X + (CONTROLS_W - imgW) / 2;
            int imgY = CONTROLS_Y + (CONTROLS_H - imgH) / 2;
            bg.drawImage(controlsImg, imgX, imgY);
        }
    }

    private void createActors() {
        // Robot — tile position set by {@link Level#setup} via {@link #installTileLevel}
        robot = new RobotActor();
        addObject(robot,
            GameAreaConfig.tileCentreX(0),
            GameAreaConfig.tileCentreY(0));

        // Code editor — centred in the script area
        editor = new CodeEditor(() -> stepScript());
        addObject(editor,
            SCRIPT_AREA_X + SCRIPT_AREA_W / 2,
            SCRIPT_AREA_Y + SCRIPT_AREA_H / 2);

        // RUN and RESET buttons in the controls area, centered as a compact row.
        int runButtonW = GameScreenLayout.scale(75);
        int resetButtonW = GameScreenLayout.scale(90);
        int buttonGap = GameScreenLayout.scale(10);
        int buttonRowW = runButtonW + buttonGap + resetButtonW;
        int buttonRowX = CONTROLS_X + (CONTROLS_W - buttonRowW) / 2;
        // Nudge controls buttons slightly higher in the panel
        int buttonY = CONTROLS_Y + GameScreenLayout.scale(46);

        int buttonH = GameScreenLayout.scale(36);

        // Try to load artwork for RUN and RESET; fall back to drawn buttons if missing.
        GreenfootImage runImg = null;
        GreenfootImage resetImg = null;
        try { runImg = new GreenfootImage("ui/run-btn.png"); } catch (IllegalArgumentException e) { runImg = null; }
        try { resetImg = new GreenfootImage("ui/reset-btn.png"); } catch (IllegalArgumentException e) { resetImg = null; }

        // Make artwork buttons a bit smaller than the allocated slot so they have padding.
        int imgRunW = (int)(runButtonW * 0.80);
        int imgResetW = (int)(resetButtonW * 0.80);
        int imgH = (int)(buttonH * 0.90);

        if (runImg != null) {
            addObject(new MenuButton(runImg, () -> runScript(), imgRunW, imgH),
                buttonRowX + runButtonW / 2,
                buttonY);
        } else {
            addObject(new MenuButton("RUN", () -> runScript(), runButtonW),
                buttonRowX + runButtonW / 2,
                buttonY);
        }

        if (resetImg != null) {
            addObject(new MenuButton(resetImg, () -> resetEditor(), imgResetW, imgH),
                buttonRowX + runButtonW + buttonGap + resetButtonW / 2,
                buttonY);
        } else {
            addObject(new MenuButton("RESET", () -> resetEditor(), resetButtonW),
                buttonRowX + runButtonW + buttonGap + resetButtonW / 2,
                buttonY);
        }

        addObject(new ClearOutputButton(() -> clearTerminal()),
            TERMINAL_X + TERMINAL_W - GameScreenLayout.scale(22),
            TERMINAL_Y + GameScreenLayout.scale(15));
    }

    // ── Act ───────────────────────────────────────────────────────────────────

    @Override
    public void act() {
        if (introOverlayActive || programEndOverlayActive) return;
        if (goalAdvanceCountdown < 0) return;
        if (goalAdvanceCountdown > 0) {
            goalAdvanceCountdown--;
        } else {
            goalAdvanceCountdown = -1;
            advanceToNextLevel();
        }
    }

    // ── Goal / level advance ──────────────────────────────────────────────────

    public void onGoalReached() {
        if (goalAdvanceCountdown >= 0) return;

        int stars = (lastProgram == null) ? 3 : calculateStars(lastProgram);
        LevelManager.recordCurrentLevelStars(stars);
        logToTerminal("~ # *** LEVEL COMPLETE! ***");
        logToTerminal("~ # " + starString(stars) + "  (" + LevelManager.getTotalStars() + "/" + LevelManager.getMaxStars() + " total)");
        goalAdvanceCountdown = 60; // ~1 s at default Greenfoot speed
    }

    private int calculateStars(Program program) {
        if (LevelManager.getCurrentLevelNumber() <= 7) {
            return 3;
        }

        int abstraction = new AbstractionScorer().score(program, totalCoins, coinsCollected, attempts);
        if (abstraction >= 75) return 3;
        if (abstraction >= 25) return 2;
        return 1;
    }

    private String starString(int stars) {
        switch (stars) {
            case 3:  return "★★★"; // ★★★
            case 2:  return "★★☆"; // ★★☆
            case 1:  return "★☆☆"; // ★☆☆
            default: return "☆☆☆"; // ☆☆☆
        }
    }

    private void advanceToNextLevel() {
        if (LevelManager.hasNextLevel()) {
            LevelManager.advanceLevel();
            Greenfoot.setWorld(new LoadingWorld(() -> new MyWorld(LevelManager.getCurrentLevel())));
        } else {
            Greenfoot.setWorld(new FinalClassificationWorld());
        }
    }

    // ── Button actions ────────────────────────────────────────────────────────

    private void runScript() {
        if (introOverlayActive || programEndOverlayActive) return;
        if (robot.isRunning()) return;

        String source = editor.getText();
        clearTerminal();
        editor.clearExecutingLine();

        if (source.trim().isEmpty()) {
            logToTerminal("~ # [!] Nothing to run");
            return;
        }

        if (source.trim().equals("skipLvl")) {
            onGoalReached();
            return;
        }

        try {
            List<Lexer.Token> tokens  = new Lexer(source).tokenize();
            Program           program = new Parser(tokens).parse();

            if (program.isEmpty()) {
                logToTerminal("~ # [!] Program is empty");
                return;
            }

            attempts++;
            lastProgram = program;
            redrawHUD();
            robot.run(program);

        } catch (Lexer.LexError e) {
            logToTerminal("~ # [LEX ERROR] " + e.getMessage());
        } catch (Parser.ParseError e) {
            logToTerminal("~ # [PARSE ERROR] " + e.getMessage());
        } catch (RuntimeException e) {
            logToTerminal("~ # [ERROR] " + e.getMessage());
        }
    }

    private void stepScript() {
        if (introOverlayActive || programEndOverlayActive) return;

        if (robot.isStepping()) {
            robot.stepOnce();
            return;
        }
        if (robot.isRunning()) return;

        String source = editor.getText();
        clearTerminal();
        editor.clearExecutingLine();

        if (source.trim().isEmpty()) {
            logToTerminal("~ # [!] Nothing to step");
            return;
        }

        try {
            List<Lexer.Token> tokens  = new Lexer(source).tokenize();
            Program           program = new Parser(tokens).parse();

            if (program.isEmpty()) {
                logToTerminal("~ # [!] Program is empty");
                return;
            }

            attempts++;
            lastProgram = program;
            redrawHUD();
            robot.startStepping(program);
            robot.stepOnce();

        } catch (Lexer.LexError e) {
            logToTerminal("~ # [LEX ERROR] " + e.getMessage());
        } catch (Parser.ParseError e) {
            logToTerminal("~ # [PARSE ERROR] " + e.getMessage());
        } catch (RuntimeException e) {
            logToTerminal("~ # [ERROR] " + e.getMessage());
        }
    }

    private void resetEditor() {
        if (introOverlayActive || programEndOverlayActive) return;
        editor.clearExecutingLine();
        robot.resetToHome();
    }

    public void showExecutingLine(int lineNumber) {
        editor.setExecutingLine(lineNumber);
    }

    public void clearExecutingLine() {
        editor.clearExecutingLine();
    }

    public void onProgramEndedWithoutGoal() {
        showProgramStopOverlay("Program ended", "The robot did not reach the goal.");
    }

    public void onProgramBlocked(String reason) {
        showProgramStopOverlay("Robot blocked", reason);
    }

    private void showProgramStopOverlay(String title, String message) {
        if (programEndOverlayActive || goalAdvanceCountdown >= 0) return;

        programEndOverlayActive = true;
        PopupOverlay.Style style = new PopupOverlay.Style(
            new Color(0, 0, 0, 160),
            new Color(241, 242, 236),
            new Color(217, 222, 214),
            new Color(42, 50, 58),
            new Color(42, 50, 58),
            Color.WHITE,
            new Color(42, 50, 58),
            GameScreenLayout.scale(62),
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

        List<PopupOverlay.Button> buttons = new ArrayList<>();
        buttons.add(new PopupOverlay.Button(
            PROGRAM_STOP_BUTTON_X,
            PROGRAM_STOP_BUTTON_Y,
            PROGRAM_STOP_BUTTON_W,
            PROGRAM_STOP_BUTTON_H,
            overlay -> "OK",
            PopupOverlay::close));

        addObject(
            new PopupOverlay(
                PROGRAM_STOP_CARD_W,
                PROGRAM_STOP_CARD_H,
                PROGRAM_STOP_CARD_X,
                PROGRAM_STOP_CARD_Y,
                style,
                (overlay, image) -> {
                    image.setColor(new Color(42, 50, 58));
                    image.setFont(new Font("SansSerif", true, false, GameScreenLayout.scale(20)));
                    image.drawString(title, overlay.getCardLeft() + GameScreenLayout.scale(24), overlay.getCardTop() + GameScreenLayout.scale(40));

                    image.setFont(new Font("SansSerif", false, false, GameScreenLayout.scale(15)));
                    image.drawString(message, overlay.getCardLeft() + GameScreenLayout.scale(24), overlay.getCardTop() + GameScreenLayout.scale(96));
                    image.drawString("Close this message to return to the start.", overlay.getCardLeft() + GameScreenLayout.scale(24), overlay.getCardTop() + GameScreenLayout.scale(120));
                },
                buttons,
                () -> {
                    robot.resetToHome();
                    programEndOverlayActive = false;
                }),
            GameScreenLayout.WORLD_WIDTH / 2,
            GameScreenLayout.WORLD_HEIGHT / 2);
    }

    public void setIntroActive(boolean active) {
        introOverlayActive = active;
    }

    // ── Terminal ──────────────────────────────────────────────────────────────

    public void logToTerminal(String message) {
        terminalLog.add(message);
        if (terminalLog.size() > MAX_TERMINAL_LINES) {
            terminalLog.remove(0);
        }
        redrawTerminal();
    }

    public void clearTerminal() {
        terminalLog.clear();
        redrawTerminal();
    }

    private void redrawTerminal() {
        GreenfootImage bg = getBackground();

        // Clear terminal panel (same fill as {@link #drawStaticUI})
        bg.setColor(new Color(36, 38, 46));
        bg.fillRect(TERMINAL_X, TERMINAL_Y, TERMINAL_W, TERMINAL_H);

        // Header
        bg.setColor(new Color(120, 120, 120));
        bg.setFont(new Font(GameScreenLayout.scale(11)));
        bg.drawString("OUTPUT", TERMINAL_X + GameScreenLayout.scale(8), TERMINAL_Y + GameScreenLayout.scale(14));

        // Log lines
        bg.setFont(new Font("Monospaced", false, false, GameScreenLayout.scale(11)));
        for (int i = 0; i < terminalLog.size(); i++) {
            bg.setColor(new Color(180, 240, 180));
            bg.drawString(terminalLog.get(i),
                TERMINAL_X + GameScreenLayout.scale(8),
                TERMINAL_Y + GameScreenLayout.scale(24) + i * TERMINAL_LINE_H);
        }
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void redrawHUD() {
        GreenfootImage bg = getBackground();

        // Prefer using an image asset for the HUD background when available.
        // Falls back to the previous solid fill color if the image can't be loaded.
        GreenfootImage hudImg = null;

        hudImg = new GreenfootImage("ui/game-top-hud-bar.png");

        // Scale the image to exactly fit the HUD strip and draw it.
        hudImg.scale(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.HUD_STRIP_H);
            bg.drawImage(hudImg, 0, 0);

        // Draw styled left-side labels (LEVEL / ATTEMPTS) using small text images so we can
        // measure and place them precisely. Colors tuned to match the provided sample.
        int yCenter = GameScreenLayout.HUD_STRIP_H / 2;

        // Nudge labels slightly down from vertical center to match sample artwork
        int verticalOffset = GameScreenLayout.scale(2);

        int labelFont = GameScreenLayout.scale(12);
        Color orange = new Color(255, 140, 30);
        Color cyan = new Color(44, 220, 215);
        Color offWhite = new Color(241, 242, 236);

        String levelLabel = "LEVEL:";
        String levelNum = String.valueOf(LevelManager.getCurrentLevelNumber());
        String attemptsLabel = "ATTEMPTS:";
        String attemptsNum = String.valueOf(attempts);

        GreenfootImage lblLevelImg = new GreenfootImage(levelLabel, labelFont, orange, new Color(0,0,0,0));
        GreenfootImage numLevelImg = new GreenfootImage(levelNum, labelFont, offWhite, new Color(0,0,0,0));
        GreenfootImage lblAttemptsImg = new GreenfootImage(attemptsLabel, labelFont, cyan, new Color(0,0,0,0));
        GreenfootImage numAttemptsImg = new GreenfootImage(attemptsNum, labelFont, offWhite, new Color(0,0,0,0));

        // Move left-side HUD group further right to match requested spacing
        int leftX = GameScreenLayout.scale(36);
        int lvlY = yCenter - lblLevelImg.getHeight()/2 + verticalOffset;
        bg.drawImage(lblLevelImg, leftX, lvlY);
        bg.drawImage(numLevelImg, leftX + lblLevelImg.getWidth() + GameScreenLayout.scale(8), lvlY);

        int attemptsX = leftX + lblLevelImg.getWidth() + numLevelImg.getWidth() + GameScreenLayout.scale(32);
        bg.drawImage(lblAttemptsImg, attemptsX, lvlY);
        bg.drawImage(numAttemptsImg, attemptsX + lblAttemptsImg.getWidth() + GameScreenLayout.scale(8), lvlY);

        // Draw centered title (e.g. "TRAINING SIMULATION") in the middle of the HUD.
        String title = "TRAINING SIMULATION";
        int titleFont = GameScreenLayout.scale(14);
        Color titleColor = new Color(150, 130, 230);
        GreenfootImage titleImg = new GreenfootImage(title, titleFont, titleColor, new Color(0,0,0,0));
        int tx = (GameScreenLayout.WORLD_WIDTH - titleImg.getWidth()) / 2;
        int ty = yCenter - titleImg.getHeight()/2 + verticalOffset;
        bg.drawImage(titleImg, tx, ty);
    }

    // ── Tiled game area (called from {@link Level} implementations) ─────────────

    /** Current level grid; null before {@link #installTileLevel}. */
    public TileMap getTileMap() {
        return tileMap;
    }

    /**
     * Paints tile backgrounds, spawns obstacle/goal/coin actors, and places the robot.
     */
    public void installTileLevel(ParsedTileLevel parsed) {
        installTileLevel(parsed.tileMap, parsed.startCol, parsed.startRow);
    }

    public void installTileLevel(TileMap map, int startCol, int startRow) {
        if (map.getCols() != GameAreaConfig.TILE_COLS || map.getRows() != GameAreaConfig.TILE_ROWS) {
            throw new IllegalStateException(
                "TileMap size " + map.getCols() + "×" + map.getRows()
                    + " does not match GameAreaConfig " + GameAreaConfig.TILE_COLS + "×"
                    + GameAreaConfig.TILE_ROWS);
        }
        this.tileMap = map;
        int count = 0;
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getCols(); c++) {
                if (map.isCoinAt(c, r)) count++;
            }
        }
        totalCoins     = count;
        coinsCollected = 0;
        GameAreaTilePainter.paintTileBackgrounds(this, map);
        TileActorLayer.spawn(this, map);
        robot.placeOnTile(startCol, startRow);
        addObject(new GameAreaFrameOverlay(),
            GameAreaConfig.GAME_AREA_WIDTH_PX / 2,
            GAME_AREA_MIN_Y + GameAreaConfig.GAME_AREA_HEIGHT_PX / 2);
    }

    /** Clears a collected coin from the map and removes its actors. */
    public void collectCoinAt(int col, int row) {
        if (tileMap == null || !tileMap.isCoinAt(col, row)) {
            return;
        }
        tileMap.getCell(col, row).setObjectKind(TileObjectKind.NONE);
        coinsCollected++;
        int x = GameAreaConfig.tileCentreX(col);
        int y = GameAreaConfig.tileCentreY(row);
        for (Coin coin : getObjectsAt(x, y, Coin.class)) {
            removeObject(coin);
        }
        logToTerminal("~ # Coin collected");
    }
}

class ClearOutputButton extends Actor {
    private final Runnable action;

    private static final int SIZE = GameScreenLayout.scale(22);

    ClearOutputButton(Runnable action) {
        this.action = action;
        setImage(createImage());
    }

    @Override
    public void act() {
        if (Greenfoot.mouseClicked(this)) {
            action.run();
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

class GameAreaFrameOverlay extends Actor {
    private static final String FRAME_IMAGE_PATH = "ui/game_area_frame.png";

    GameAreaFrameOverlay() {
        setImage(createImage());
    }

    private GreenfootImage createImage() {
        GreenfootImage frame = new GreenfootImage(FRAME_IMAGE_PATH);
        if (frame.getWidth() != GameAreaConfig.GAME_AREA_WIDTH_PX
                || frame.getHeight() != GameAreaConfig.GAME_AREA_HEIGHT_PX) {
            frame.scale(GameAreaConfig.GAME_AREA_WIDTH_PX, GameAreaConfig.GAME_AREA_HEIGHT_PX);
        }
        return frame;
    }
}
