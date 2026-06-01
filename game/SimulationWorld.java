import greenfoot.*;
import java.util.List;

/**
 * SimulationWorld
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
public class SimulationWorld extends World {

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

    private static final GreenfootImage TERMINAL_BACKGROUND = loadTerminalBackground();

    // Terminal rendering delegated to TerminalManager

    // ── State ─────────────────────────────────────────────────────────────────

    private RobotActor  robot;
    private CodeEditor  editor;
    private Level       level;
    private TileMap     tileMap;
    private final MyWorldSessionState sessionState;

    private final TerminalManager terminalManager;
    private final ProgramCompilationPipeline compilationPipeline;
    private CheatEngine cheatEngine;
    private ProgramExecutionService executionService;
    private LevelProgressionController progressionController;
    private ProgramStopOverlayController overlayController;
    private MyWorldCoinTracker coinTracker;
    private TileLevelInstaller tileLevelInstaller;

    // ── Constructor ───────────────────────────────────────────────────────────

    public SimulationWorld(Level level) {
        super(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT, 1);
        this.level = level;
        this.sessionState = new MyWorldSessionState();
        // initialize managers used by UI and actors
        this.terminalManager = new TerminalManager(this);
        this.compilationPipeline = new ProgramCompilationPipeline();

        this.cheatEngine = new CheatEngine(terminalManager);
        this.cheatEngine.register("skip",  args -> onGoalReached());
        this.cheatEngine.register("goto",     args -> goToLevel(Integer.parseInt(args[0])));
        this.cheatEngine.register("coins",    args -> collectAllCoins());
        this.cheatEngine.register("stars",    args -> LevelManager.recordCurrentLevelStars(3));
        this.cheatEngine.register("resetAll", args -> { LevelManager.resetProgress(); goToLevel(1); });
        drawStaticUI();
        createActors();
        this.executionService = new ProgramExecutionService(
            robot,
            editor,
            terminalManager,
            compilationPipeline,
            sessionState,
            cheatEngine,
            this::redrawHUD,
            this::onGoalReached);
        this.progressionController = new LevelProgressionController(sessionState, terminalManager);
        this.overlayController = new ProgramStopOverlayController(this, robot, sessionState);
        this.coinTracker = new MyWorldCoinTracker(sessionState);
        this.tileLevelInstaller = new TileLevelInstaller(this, robot, coinTracker);

        level.setup(this);
        terminalManager.redraw();
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

        // Terminal panel (asset-backed when available)
        drawTerminalBackground(bg);

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
        editor = new CodeEditor(() -> stepScript(), cheatEngine::tryDispatch);
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
    }

    // ── Act ───────────────────────────────────────────────────────────────────

    @Override
    public void act() {
        if (progressionController.tickAdvanceCountdown(
            sessionState.isIntroOverlayActive()
                || sessionState.isProgramEndOverlayActive()
                || sessionState.isLevelCompleteActive())) {
            showLevelCompleteOverlay();
        }
    }

    // ── Goal / level advance ──────────────────────────────────────────────────

    public void onGoalReached() {
        progressionController.onGoalReached();
    }

    /**
     * Shown when the win-pause countdown fires. Presents the interactive
     * Level Complete panel; navigation is driven by the panel's buttons.
     */
    private void showLevelCompleteOverlay() {
        addObject(
            new LevelCompleteOverlay(
                this,
                LevelManager.getCurrentLevelStars(),
                LevelManager.getCurrentLevelNumber(),
                sessionState.getAttempts()),
            GameScreenLayout.WORLD_WIDTH / 2,
            GameScreenLayout.WORLD_HEIGHT / 2);
    }

    public void goHome() {
        Greenfoot.setWorld(new HomeWorld());
    }

    public void replayLevel() {
        Greenfoot.setWorld(new LoadingWorld(() -> new SimulationWorld(LevelManager.getCurrentLevel())));
    }

    public void advanceToNextLevel() {
        if (LevelManager.hasNextLevel()) {
            LevelManager.advanceLevel();
            Greenfoot.setWorld(new LoadingWorld(() -> new SimulationWorld(LevelManager.getCurrentLevel())));
        } else {
            Greenfoot.setWorld(new FinalClassificationWorld());
        }
    }

    // ── Button actions ────────────────────────────────────────────────────────

    private void runScript() {
        executionService.runScript();
    }

    private void stepScript() {
        executionService.stepScript();
    }

    private void resetEditor() {
        executionService.resetEditor();
    }

    public void showExecutingLine(int lineNumber) {
        editor.setExecutingLine(lineNumber);
    }

    public void clearExecutingLine() {
        editor.clearExecutingLine();
    }

    public void onProgramEndedWithoutGoal() {
        overlayController.showProgramEndedWithoutGoal();
    }

    public void onProgramBlocked(String reason) {
        overlayController.showProgramBlocked(reason);
    }

    public void setIntroActive(boolean active) {
        sessionState.setIntroOverlayActive(active);
    }

    public void setLevelCompleteActive(boolean active) {
        sessionState.setLevelCompleteActive(active);
    }

    // Terminal rendering is handled by TerminalManager.

    private static GreenfootImage loadTerminalBackground() {
        try {
            GreenfootImage image = new GreenfootImage("ui/output_background.png");
            if (image.getWidth() != TERMINAL_W || image.getHeight() != TERMINAL_H) {
                image.scale(TERMINAL_W, TERMINAL_H);
            }
            return image;
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    void drawTerminalBackground(GreenfootImage bg) {
        if (TERMINAL_BACKGROUND != null) {
            bg.drawImage(TERMINAL_BACKGROUND, TERMINAL_X, TERMINAL_Y);
            return;
        }

        bg.setColor(new Color(36, 38, 46));
        bg.fillRect(TERMINAL_X, TERMINAL_Y, TERMINAL_W, TERMINAL_H);
    }

    public void logToTerminal(String line) {
        terminalManager.log(line);
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
        String attemptsNum = String.valueOf(sessionState.getAttempts());

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
        this.tileMap = map;
        tileLevelInstaller.install(map, startCol, startRow);
    }

    /** Clears a collected coin from the map and removes its actors. */
    public void collectCoinAt(int col, int row) {
        coinTracker.collectCoinAt(this, tileMap, col, row);
    }

    private void collectAllCoins() {
        for (int r = 0; r < tileMap.getRows(); r++) {
            for (int c = 0; c < tileMap.getCols(); c++) {
                if (tileMap.isCoinAt(c, r)) {
                    collectCoinAt(c, r);
                }
            }
        }
    }

    private void goToLevel(int n) {
        LevelManager.setCurrentLevel(n - 1);
        Greenfoot.setWorld(new LoadingWorld(() -> new SimulationWorld(LevelManager.getCurrentLevel())));
    }
}
