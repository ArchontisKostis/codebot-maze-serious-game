public final class MyWorldCoinTracker {

    private final MyWorldSessionState sessionState;

    public MyWorldCoinTracker(MyWorldSessionState sessionState) {
        this.sessionState = sessionState;
    }

    public void initializeForMap(TileMap map) {
        int count = 0;
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                if (map.isCoinAt(col, row)) {
                    count++;
                }
            }
        }
        sessionState.setTotalCoins(count);
        sessionState.setCoinsCollected(0);
    }

    public void collectCoinAt(SimulationWorld world, TileMap tileMap, int col, int row) {
        if (tileMap == null || !tileMap.isCoinAt(col, row)) {
            return;
        }

        tileMap.getCell(col, row).setObjectKind(TileObjectKind.NONE);
        sessionState.incrementCoinsCollected();

        int x = GameAreaConfig.tileCentreX(col);
        int y = GameAreaConfig.tileCentreY(row);
        for (Coin coin : world.getObjectsAt(x, y, Coin.class)) {
            world.removeObject(coin);
        }
        world.logToTerminal("~ # Coin collected");
    }
}