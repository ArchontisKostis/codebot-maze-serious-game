import greenfoot.Actor;
import greenfoot.World;

/**
 * Spawns visual {@link Actor}s for each {@link TileObjectKind} on the map.
 * Logic is driven by {@link TileMap}; actors sit centred on their tile.
 */
public final class TileActorLayer {

    private TileActorLayer() {
    }

    public static void spawn(World world, TileMap map) {
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                TileObjectKind kind = map.getCell(col, row).getObjectKind();
                Actor actor = createActor(kind);
                if (actor != null) {
                    world.addObject(actor,
                        GameAreaConfig.tileCentreX(col),
                        GameAreaConfig.tileCentreY(row));
                }
            }
        }
    }

    private static Actor createActor(TileObjectKind kind) {
        switch (kind) {
            case OBSTACLE:
                return new Obstacle();
            case GOAL:
                return new Goal();
            case COIN:
                return new Coin();
            case NONE:
            default:
                return null;
        }
    }
}
