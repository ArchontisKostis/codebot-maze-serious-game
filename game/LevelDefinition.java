/**
 * The data a level needs, independent of where it came from — a built-in campaign
 * level or a custom level pasted/loaded at runtime. Produced by
 * {@link LevelDocumentParser} from a {@code .lvl} document and consumed by
 * {@link SimulationWorld}.
 *
 * <p>The grid comes from {@link AsciiTileMapParser} (so {@code .lvl} bodies reuse the
 * exact tile parser the game already trusts); the header adds optional presentation
 * and scoring metadata. Absent header fields fall back to defaults — completion
 * scoring with 25/75 star thresholds and no name/author.
 */
public final class LevelDefinition {

    /** Default abstraction thresholds for ★★ and ★★★ when a document omits {@code stars}. */
    public static final int DEFAULT_TWO_STAR = 25;
    public static final int DEFAULT_THREE_STAR = 75;

    public final TileMap tileMap;
    public final int startCol;
    public final int startRow;

    /** May be null/empty — display name from the {@code name} header. */
    public final String name;
    /** May be null/empty — author credit from the {@code author} header. */
    public final String author;

    public final ScorerKind scorer;
    public final int twoStarThreshold;
    public final int threeStarThreshold;

    public LevelDefinition(TileMap tileMap, int startCol, int startRow,
            String name, String author,
            ScorerKind scorer, int twoStarThreshold, int threeStarThreshold) {
        this.tileMap = tileMap;
        this.startCol = startCol;
        this.startRow = startRow;
        this.name = name;
        this.author = author;
        this.scorer = scorer;
        this.twoStarThreshold = twoStarThreshold;
        this.threeStarThreshold = threeStarThreshold;
    }

    public boolean hasName() {
        return name != null && !name.isEmpty();
    }

    /** Returns a copy with completion scoring forced — used so Free Play ignores a custom document's scorer. */
    public LevelDefinition withCompletionScoring() {
        if (scorer == ScorerKind.COMPLETION) {
            return this;
        }
        return new LevelDefinition(tileMap, startCol, startRow, name, author,
            ScorerKind.COMPLETION, DEFAULT_TWO_STAR, DEFAULT_THREE_STAR);
    }
}
