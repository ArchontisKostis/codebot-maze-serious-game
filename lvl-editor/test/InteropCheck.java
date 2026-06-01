import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Interop check (task 5.2): proves the web editor's exported samples are accepted
 * by the game's real loader, in both forms.
 *
 * Loads each sample's raw {@code .lvl} text AND its unwrapped base64 share-code
 * through {@link LevelDocumentParser#load(String)} and asserts both succeed.
 * Compile alongside the game sources (default package) and run with the samples
 * directory as the single argument:
 *
 *   java InteropCheck lvl-editor/samples
 */
public final class InteropCheck {

    private static final String[] SAMPLE_IDS = { "named", "grid-only" };

    public static void main(String[] args) throws Exception {
        Path dir = Paths.get(args.length > 0 ? args[0] : "lvl-editor/samples");
        boolean allOk = true;

        for (String id : SAMPLE_IDS) {
            String lvl = read(dir.resolve(id + ".lvl"));
            String b64 = read(dir.resolve(id + ".b64"));

            LevelLoadResult raw = LevelDocumentParser.load(lvl);
            LevelLoadResult code = LevelDocumentParser.load(b64);

            boolean ok = raw.isOk() && code.isOk() && sameContent(raw, code);
            allOk = allOk && ok;

            System.out.println(id + ".lvl   raw=" + describe(raw));
            System.out.println(id + ".b64   base64=" + describe(code));
        }

        System.out.println(allOk ? "INTEROP: PASS" : "INTEROP: FAIL");
        if (!allOk) {
            throw new IllegalStateException("Editor samples were not accepted by the game loader.");
        }
    }

    private static String read(Path p) throws Exception {
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    private static String describe(LevelLoadResult r) {
        if (!r.isOk()) {
            return "ERR(" + r.getError() + ")";
        }
        LevelDefinition d = r.getDefinition();
        return "OK start=(" + d.startCol + "," + d.startRow + ") goals=" + count(d, TileObjectKind.GOAL)
            + " coins=" + count(d, TileObjectKind.COIN) + " name=" + d.name;
    }

    private static boolean sameContent(LevelLoadResult a, LevelLoadResult b) {
        LevelDefinition da = a.getDefinition();
        LevelDefinition db = b.getDefinition();
        return da.startCol == db.startCol && da.startRow == db.startRow
            && count(da, TileObjectKind.GOAL) == count(db, TileObjectKind.GOAL)
            && count(da, TileObjectKind.COIN) == count(db, TileObjectKind.COIN);
    }

    private static int count(LevelDefinition d, TileObjectKind kind) {
        TileMap m = d.tileMap;
        int n = 0;
        for (int r = 0; r < m.getRows(); r++) {
            for (int c = 0; c < m.getCols(); c++) {
                if (m.getCell(c, r).getObjectKind() == kind) {
                    n++;
                }
            }
        }
        return n;
    }
}
