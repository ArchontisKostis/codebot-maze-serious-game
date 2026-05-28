import greenfoot.*;

/**
 * RobotActor
 *
 * Programmable robot on a discrete tile grid: each move command steps exactly one
 * tile. Collision and goals use {@link TileMap}, not pixel overlap.
 */
public class RobotActor extends Actor {

    private final Interpreter interpreter = new Interpreter();
    private long executionStartTime;

    private int tileCol;
    private int tileRow;
    private int homeCol;
    private int homeRow;
    private boolean goalAnnouncedThisRun;
    private boolean autoRunning;
    private boolean stepping;

    public RobotActor() {
        applyTileSizedSprite();
    }

    /**
     * Greenfoot often assigns the class image after construction; rescale whenever we enter a world.
     */
    @Override
    public void addedToWorld(World world) {
        applyTileSizedSprite();
    }

    /** Forces the sprite to exactly one tile ({@link GameAreaConfig#TILE_SIZE_PX}). */
    private void applyTileSizedSprite() {
        int s = GameAreaConfig.TILE_SIZE_PX;
        GreenfootImage img = null;
        img = new GreenfootImage("robot/robot.png");

        setImage(img);
    }

    @Override
    public void act() {
        if (!autoRunning || !interpreter.isRunning()) return;
        advanceOneStep(true);
    }

    public void startStepping(Program program) {
        if (interpreter.isRunning()) return;
        resetToHome();
        goalAnnouncedThisRun = false;
        autoRunning = false;
        stepping = true;
        executionStartTime = System.currentTimeMillis();
        interpreter.load(program);
        world().logToTerminal("~ # Step debugger ready");
    }

    public void stepOnce() {
        if (autoRunning || !interpreter.isRunning()) return;
        advanceOneStep(false);
        if (!interpreter.isRunning()) {
            stepping = false;
        }
    }

    public boolean isStepping() {
        return stepping && interpreter.isRunning();
    }

    private void advanceOneStep(boolean delayAfterMove) {
        MoveStatement move = interpreter.next();

        if (move == null) {
            world().clearExecutingLine();
            autoRunning = false;
            stepping = false;
            long elapsed = System.currentTimeMillis() - executionStartTime;
            world().logToTerminal("~ # Done in " + elapsed + " ms");
            world().onProgramEndedWithoutGoal();
            return;
        }

        world().showExecutingLine(move.sourceLine);
        world().logToTerminal("~ # " + move.label());
        executeMove(move.direction);

        if (delayAfterMove) {
            Greenfoot.delay(Settings.getAnimationDelay());
        }
    }

    /**
     * Places the robot on a tile and records that tile as home for RUN / RESET.
    * Called from {@link SimulationWorld#installTileLevel}.
     */
    public void placeOnTile(int col, int row) {
        tileCol = col;
        tileRow = row;
        homeCol = col;
        homeRow = row;
        syncPixelsFromTile();
    }

    /** Teleports the robot back to its level starting tile. */
    public void resetToHome() {
        interpreter.halt();
        autoRunning = false;
        stepping = false;
        tileCol = homeCol;
        tileRow = homeRow;
        goalAnnouncedThisRun = false;
        if (getWorld() != null) {
            world().clearExecutingLine();
        }
        syncPixelsFromTile();
    }

    public void run(Program program) {
        if (interpreter.isRunning()) return;
        resetToHome();
        goalAnnouncedThisRun = false;
        autoRunning = true;
        stepping = false;
        executionStartTime = System.currentTimeMillis();
        interpreter.load(program);
        world().logToTerminal("~ # Running...");
    }

    public boolean isRunning() {
        return interpreter.isRunning();
    }

    private void executeMove(MoveStatement.Direction dir) {
        int dc = 0;
        int dr = 0;
        switch (dir) {
            case UP:
                dr = -1;
                break;
            case DOWN:
                dr = 1;
                break;
            case LEFT:
                dc = -1;
                break;
            case RIGHT:
                dc = 1;
                break;
        }

        int nextCol = tileCol + dc;
        int nextRow = tileRow + dr;
        TileMap map = world().getTileMap();

        if (map == null || !map.isWalkable(nextCol, nextRow)) {
            interpreter.halt();
            autoRunning = false;
            stepping = false;
            if (map == null || !GameAreaConfig.isInsideGrid(nextCol, nextRow)) {
                world().logToTerminal("~ # [!] Can't leave the game area");
                world().onProgramBlocked("The robot tried to leave the game area.");
            } else {
                world().logToTerminal("~ # [!] Blocked by obstacle");
                world().onProgramBlocked("The robot ran into an obstacle.");
            }
            return;
        }

        tileCol = nextCol;
        tileRow = nextRow;
        syncPixelsFromTile();

        if (map.isCoinAt(tileCol, tileRow)) {
            world().collectCoinAt(tileCol, tileRow);
        }

        if (map.isGoalAt(tileCol, tileRow) && !goalAnnouncedThisRun) {
            goalAnnouncedThisRun = true;
            interpreter.halt();
            autoRunning = false;
            stepping = false;
            world().logToTerminal("~ # *** LEVEL COMPLETE! ***");
            world().onGoalReached();
        }
    }

    private void syncPixelsFromTile() {
        setLocation(GameAreaConfig.tileCentreX(tileCol), GameAreaConfig.tileCentreY(tileRow));
    }

    private SimulationWorld world() {
        return (SimulationWorld) getWorld();
    }
}
