/**
 * Which star-scoring rule applies to a level.
 *
 * <ul>
 *   <li>{@link #COMPLETION} — awards full stars for reaching the goal (used by the
 *       early campaign levels and by all custom/Free Play levels).</li>
 *   <li>{@link #ABSTRACTION} — grades on {@link AbstractionScorer} (repeat usage and
 *       nesting depth) against per-level thresholds.</li>
 * </ul>
 */
public enum ScorerKind {
    COMPLETION,
    ABSTRACTION;

    /** Parses a header value ({@code completion}/{@code abstraction}), case-insensitive. */
    public static ScorerKind fromHeader(String value) {
        String v = value.trim().toLowerCase();
        switch (v) {
            case "completion": return COMPLETION;
            case "abstraction": return ABSTRACTION;
            default:
                throw new IllegalArgumentException(
                    "Unknown scorer '" + value + "' (expected completion or abstraction)");
        }
    }
}
