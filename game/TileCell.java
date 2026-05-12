/**
 * One tile: a background texture plus at most one logical object on top.
 */
public final class TileCell {

    private final TileBackgroundId backgroundId;
    private TileObjectKind objectKind;

    public TileCell(TileBackgroundId backgroundId, TileObjectKind objectKind) {
        this.backgroundId = backgroundId;
        this.objectKind = objectKind;
    }

    public TileBackgroundId getBackgroundId() {
        return backgroundId;
    }

    public TileObjectKind getObjectKind() {
        return objectKind;
    }

    public void setObjectKind(TileObjectKind objectKind) {
        this.objectKind = objectKind;
    }
}
