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
 *   - RESET button: clears editor + terminal
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

    // ── Constructor ───────────────────────────────────────────────────────────

    public MyWorld(Level level) {
        super(GameScreenLayout.WORLD_WIDTH, GameScreenLayout.WORLD_HEIGHT, 1);
        this.level = level;

        drawStaticUI();
        createActors();

        level.setup(this);
        redrawTerminal();
        redrawHUD();
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

        // Controls area — dark blue panel
        bg.setColor(new Color(35, 35, 55));
        bg.fillRect(CONTROLS_X, CONTROLS_Y, CONTROLS_W, CONTROLS_H);
        bg.setColor(new Color(140, 140, 210));
        bg.setFont(new Font(GameScreenLayout.scale(11)));
        bg.drawString("CONTROLS", CONTROLS_X + GameScreenLayout.scale(8), CONTROLS_Y + GameScreenLayout.scale(14));
    }

    private void createActors() {
        // Robot — tile position set by {@link Level#setup} via {@link #installTileLevel}
        robot = new RobotActor();
        addObject(robot,
            GameAreaConfig.tileCentreX(0),
            GameAreaConfig.tileCentreY(0));

        // Code editor — centred in the script area
        editor = new CodeEditor();
        addObject(editor,
            SCRIPT_AREA_X + SCRIPT_AREA_W / 2,
            SCRIPT_AREA_Y + SCRIPT_AREA_H / 2);

        // RUN and RESET buttons in the controls area
        addObject(new MenuButton("RUN",   () -> runScript()),
            CONTROLS_X + GameScreenLayout.CONTROLS_BTN_A_OFFSET_X,
            CONTROLS_Y + GameScreenLayout.CONTROLS_BTN_OFFSET_Y);
        addObject(new MenuButton("RESET", () -> resetEditor()),
            CONTROLS_X + GameScreenLayout.CONTROLS_BTN_B_OFFSET_X,
            CONTROLS_Y + GameScreenLayout.CONTROLS_BTN_OFFSET_Y);
    }

    // ── Act ───────────────────────────────────────────────────────────────────

    @Override
    public void act() {
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
        goalAdvanceCountdown = 60; // ~1 s at default Greenfoot speed
    }

    private void advanceToNextLevel() {
        if (LevelManager.hasNextLevel()) {
            LevelManager.advanceLevel();
            Greenfoot.setWorld(new MyWorld(LevelManager.getCurrentLevel()));
        } else {
            LevelManager.resetToFirstLevel();
            Greenfoot.setWorld(new HomeWorld());
        }
    }

    // ── Button actions ────────────────────────────────────────────────────────

    private void runScript() {
        if (robot.isRunning()) return;

        String source = editor.getText().trim();
        clearTerminal();

        if (source.isEmpty()) {
            logToTerminal("~ # [!] Nothing to run");
            return;
        }

        if (source.equals("skipLvl")) {
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

    private void resetEditor() {
        robot.resetToHome();
        editor.clear();
        clearTerminal();
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

        bg.setColor(new Color(20, 20, 35));
        bg.fillRect(0, 0, GameScreenLayout.WORLD_WIDTH, GameScreenLayout.HUD_STRIP_H);

        bg.setFont(new Font(GameScreenLayout.scale(12)));
        bg.setColor(new Color(200, 200, 200));
        bg.drawString(
            "Level: " + LevelManager.getCurrentLevelNumber() + "   Attempts: " + attempts,
            GameScreenLayout.scale(10),
            GameScreenLayout.scale(14));
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
        GameAreaTilePainter.paintTileBackgrounds(this, map);
        TileActorLayer.spawn(this, map);
        robot.placeOnTile(startCol, startRow);
    }

    /** Clears a collected coin from the map and removes its actors. */
    public void collectCoinAt(int col, int row) {
        if (tileMap == null || !tileMap.isCoinAt(col, row)) {
            return;
        }
        tileMap.getCell(col, row).setObjectKind(TileObjectKind.NONE);
        int x = GameAreaConfig.tileCentreX(col);
        int y = GameAreaConfig.tileCentreY(row);
        for (Coin coin : getObjectsAt(x, y, Coin.class)) {
            removeObject(coin);
        }
        logToTerminal("~ # Coin collected");
    }
}
