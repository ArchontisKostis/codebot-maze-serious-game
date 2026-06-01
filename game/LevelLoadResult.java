/**
 * Outcome of loading a level from untrusted input (pasted {@code .lvl} text or a
 * base64 share-code). Carries either a parsed {@link LevelDefinition} or a readable
 * error message — never both — so callers (e.g. the Load Custom screen) can surface
 * a message instead of crashing on malformed input.
 */
public final class LevelLoadResult {

    private final LevelDefinition definition;
    private final String error;

    private LevelLoadResult(LevelDefinition definition, String error) {
        this.definition = definition;
        this.error = error;
    }

    public static LevelLoadResult ok(LevelDefinition definition) {
        return new LevelLoadResult(definition, null);
    }

    public static LevelLoadResult fail(String error) {
        return new LevelLoadResult(null, error);
    }

    public boolean isOk() {
        return definition != null;
    }

    /** Non-null only when {@link #isOk()} is true. */
    public LevelDefinition getDefinition() {
        return definition;
    }

    /** Non-null only when {@link #isOk()} is false. */
    public String getError() {
        return error;
    }
}
