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
        GreenfootImage img = getImage();
        if (img != null && img.getWidth() > 0) {
            img = new GreenfootImage(img);
            img.scale(s, s);
        } else {
            img = new GreenfootImage(s, s);
            img.setColor(new Color(80, 200, 255));
            img.fill();
            img.setColor(Color.WHITE);
            img.drawRect(0, 0, s - 1, s - 1);
        }
        setImage(img);
    }

    @Override
    public void act() {
        if (!interpreter.isRunning()) return;

        MoveStatement move = interpreter.next();

        if (move == null) {
            long elapsed = System.currentTimeMillis() - executionStartTime;
            world().logToTerminal("~ # Done in " + elapsed + " ms");
            return;
        }

        world().logToTerminal("~ # " + move.label());
        executeMove(move.direction);

        Greenfoot.delay(15);
    }

    /**
     * Places the robot on a tile and records that tile as home for RUN / RESET.
     * Called from {@link MyWorld#installTileLevel}.
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
        tileCol = homeCol;
        tileRow = homeRow;
        goalAnnouncedThisRun = false;
        syncPixelsFromTile();
    }

    public void run(Program program) {
        if (interpreter.isRunning()) return;
        resetToHome();
        goalAnnouncedThisRun = false;
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
            if (map == null || !GameAreaConfig.isInsideGrid(nextCol, nextRow)) {
                world().logToTerminal("~ # [!] Can't leave the game area");
            } else {
                world().logToTerminal("~ # [!] Blocked by obstacle");
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
            world().logToTerminal("~ # *** LEVEL COMPLETE! ***");
            goalAnnouncedThisRun = true;
        }
    }

    private void syncPixelsFromTile() {
        setLocation(GameAreaConfig.tileCentreX(tileCol), GameAreaConfig.tileCentreY(tileRow));
    }

    private MyWorld world() {
        return (MyWorld) getWorld();
    }
}
