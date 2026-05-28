public final class TileLevelInstaller {

    private final SimulationWorld world;
    private final RobotActor robot;
    private final MyWorldCoinTracker coinTracker;

    public TileLevelInstaller(SimulationWorld world, RobotActor robot, MyWorldCoinTracker coinTracker) {
        this.world = world;
        this.robot = robot;
        this.coinTracker = coinTracker;
    }

    public void install(TileMap map, int startCol, int startRow) {
        if (map.getCols() != GameAreaConfig.TILE_COLS || map.getRows() != GameAreaConfig.TILE_ROWS) {
            throw new IllegalStateException(
                "TileMap size " + map.getCols() + "×" + map.getRows()
                    + " does not match GameAreaConfig " + GameAreaConfig.TILE_COLS + "×"
                    + GameAreaConfig.TILE_ROWS);
        }

        coinTracker.initializeForMap(map);
        GameAreaTilePainter.paintTileBackgrounds(world, map);
        TileActorLayer.spawn(world, map);
        robot.placeOnTile(startCol, startRow);
        world.addObject(new GameAreaFrameOverlay(),
            GameAreaConfig.GAME_AREA_WIDTH_PX / 2,
            SimulationWorld.GAME_AREA_MIN_Y + GameAreaConfig.GAME_AREA_HEIGHT_PX / 2);
    }
}