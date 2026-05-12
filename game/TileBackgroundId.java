/**
 * Identifies the painted background for a cell. Extend this enum when you add
 * new terrain art; {@link TileBackgroundRegistry} maps ids to images.
 */
public enum TileBackgroundId {
    /** Open floor — neutral walkable tile. */
    FLOOR,
    /** Solid wall / blocked terrain texture. */
    WALL
}
