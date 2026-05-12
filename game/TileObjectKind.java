/**
 * Interactive or blocking content on a tile, layered above the background.
 * The authoritative copy lives in {@link TileCell} / {@link TileMap}; actors
 * are spawned as visuals and for optional Greenfoot interactions (e.g. coins).
 */
public enum TileObjectKind {
    NONE,
    OBSTACLE,
    GOAL,
    COIN
}
