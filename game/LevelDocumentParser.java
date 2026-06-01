import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Parses a {@code .lvl} level document into a {@link LevelDefinition}.
 *
 * <p>A document is an optional header block of {@code key: value} lines, a {@code ---}
 * separator, then the 24×20 ASCII grid body. The grid body is handed to
 * {@link AsciiTileMapParser} unchanged; this class only adds the thin header reader.
 * Recognized header keys are {@code name}, {@code author}, {@code scorer}
 * ({@code completion}/{@code abstraction}), and {@code stars} ({@code two,three}).
 * Unknown keys are ignored.
 *
 * <p>Both built-in levels (embedded document strings) and custom levels (pasted text or
 * base64) flow through here, so there is one format and one loader.
 */
public final class LevelDocumentParser {

    private static final String SEPARATOR = "---";

    private LevelDocumentParser() {
    }

    /**
     * Parses a raw {@code .lvl} document. Throws {@link IllegalArgumentException} on a
     * malformed header or grid (the latter via {@link AsciiTileMapParser}). Built-in
     * levels call this directly; untrusted input should go through {@link #load(String)}.
     */
    public static LevelDefinition parse(String document) {
        if (document == null) {
            throw new IllegalArgumentException("Level document is empty.");
        }

        String[] allLines = document.split("\r?\n", -1);

        List<String> headerLines = new ArrayList<>();
        List<String> gridLines = new ArrayList<>();
        boolean inGrid = false;
        boolean sawSeparator = false;
        for (String line : allLines) {
            if (!inGrid && line.trim().equals(SEPARATOR)) {
                inGrid = true;
                sawSeparator = true;
                continue;
            }
            if (inGrid) {
                gridLines.add(line);
            } else {
                headerLines.add(line);
            }
        }

        // Header-less document: the whole text is the grid.
        if (!sawSeparator) {
            gridLines = new ArrayList<>();
            for (String line : allLines) {
                gridLines.add(line);
            }
            headerLines.clear();
        }

        // Drop trailing blank grid rows (from a trailing newline) so a clean document
        // does not exceed the 20-row limit AsciiTileMapParser enforces.
        while (!gridLines.isEmpty() && gridLines.get(gridLines.size() - 1).trim().isEmpty()) {
            gridLines.remove(gridLines.size() - 1);
        }

        Header header = readHeader(headerLines);
        ParsedTileLevel parsed = AsciiTileMapParser.parse(gridLines.toArray(new String[0]));

        return new LevelDefinition(
            parsed.tileMap, parsed.startCol, parsed.startRow,
            header.name, header.author,
            header.scorer, header.twoStar, header.threeStar);
    }

    /**
     * Loads a level from untrusted input. Input containing a line break is treated as
     * raw {@code .lvl} text; single-line input is base64-decoded first. All failures are
     * returned as a {@link LevelLoadResult} error rather than thrown.
     */
    public static LevelLoadResult load(String input) {
        if (input == null || input.trim().isEmpty()) {
            return LevelLoadResult.fail("No level provided.");
        }

        String document;
        if (containsLineBreak(input.trim())) {
            document = input;
        } else {
            try {
                byte[] decoded = Base64.getDecoder().decode(input.trim());
                document = new String(decoded, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                return LevelLoadResult.fail("Could not decode share-code (invalid base64).");
            }
        }

        try {
            return LevelLoadResult.ok(parse(document));
        } catch (RuntimeException e) {
            return LevelLoadResult.fail("Could not load level: " + e.getMessage());
        }
    }

    private static boolean containsLineBreak(String s) {
        return s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
    }

    private static Header readHeader(List<String> headerLines) {
        Header h = new Header();
        for (String raw : headerLines) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            int colon = line.indexOf(':');
            if (colon < 0) {
                continue; // tolerate stray lines
            }
            String key = line.substring(0, colon).trim().toLowerCase();
            String value = line.substring(colon + 1).trim();
            switch (key) {
                case "name":
                    h.name = value;
                    break;
                case "author":
                    h.author = value;
                    break;
                case "scorer":
                    h.scorer = ScorerKind.fromHeader(value);
                    break;
                case "stars":
                    applyStars(h, value);
                    break;
                default:
                    // ignore unknown keys
                    break;
            }
        }
        return h;
    }

    private static void applyStars(Header h, String value) {
        String[] parts = value.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                "stars must be 'two,three' (e.g. 25,75), got '" + value + "'");
        }
        try {
            h.twoStar = Integer.parseInt(parts[0].trim());
            h.threeStar = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "stars values must be integers, got '" + value + "'");
        }
    }

    private static final class Header {
        String name = null;
        String author = null;
        ScorerKind scorer = ScorerKind.COMPLETION;
        int twoStar = LevelDefinition.DEFAULT_TWO_STAR;
        int threeStar = LevelDefinition.DEFAULT_THREE_STAR;
    }

    // ── Self-test (task 1.6): .lvl → base64 → decode round-trip ───────────────────

    /**
     * Standalone verification harness. Compiles into the project but is only run
     * manually (e.g. {@code java LevelDocumentParser}); the game never calls it.
     */
    public static void main(String[] args) {
        String document = String.join("\n",
            "name: Round Trip",
            "author: Test",
            "scorer: abstraction",
            "stars: 30,80",
            "---",
            "########################",
            "#S....................G#",
            "#..........C...........#",
            "########################");

        LevelDefinition direct = parse(document);

        String base64 = Base64.getEncoder().encodeToString(document.getBytes(StandardCharsets.UTF_8));
        LevelLoadResult viaCode = load(base64);
        LevelLoadResult viaRaw = load(document);

        boolean ok = viaCode.isOk() && viaRaw.isOk()
            && sameDefinition(direct, viaCode.getDefinition())
            && sameDefinition(direct, viaRaw.getDefinition());

        // A few negative cases should fail gracefully (no exceptions thrown out).
        boolean negatives =
            !load("not base64 at all !!!").isOk()
            && !load("---\n###\n#G#\n###").isOk()       // no start
            && !load(noGoalDocument()).isOk();          // exactly-one-G guard

        System.out.println("round-trip: " + (ok ? "PASS" : "FAIL"));
        System.out.println("negatives:  " + (negatives ? "PASS" : "FAIL"));
        if (!ok || !negatives) {
            throw new IllegalStateException("LevelDocumentParser self-test failed.");
        }
    }

    private static String noGoalDocument() {
        return String.join("\n", "---",
            "########################",
            "#S.....................#",
            "########################");
    }

    private static boolean sameDefinition(LevelDefinition a, LevelDefinition b) {
        if (a.startCol != b.startCol || a.startRow != b.startRow) return false;
        if (a.scorer != b.scorer) return false;
        if (a.twoStarThreshold != b.twoStarThreshold) return false;
        if (a.threeStarThreshold != b.threeStarThreshold) return false;
        if (!java.util.Objects.equals(a.name, b.name)) return false;
        if (!java.util.Objects.equals(a.author, b.author)) return false;
        TileMap ma = a.tileMap;
        TileMap mb = b.tileMap;
        if (ma.getCols() != mb.getCols() || ma.getRows() != mb.getRows()) return false;
        for (int r = 0; r < ma.getRows(); r++) {
            for (int c = 0; c < ma.getCols(); c++) {
                TileCell ca = ma.getCell(c, r);
                TileCell cb = mb.getCell(c, r);
                if (ca.getBackgroundId() != cb.getBackgroundId()
                    || ca.getObjectKind() != cb.getObjectKind()) {
                    return false;
                }
            }
        }
        return true;
    }
}
