import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class LevelIntroRegistry {
    private static final String INTRO_DIR = "level-intros";

    private static final LevelIntroData[] FALLBACK_DATA = {
        data("Linear Traversal",
            "RIVETS awaits its first instruction. Show it the way forward.",
            "moveRight",
            "moveRight",
            "Guide RIVETS to the goal tile."),
        data("Angular Shift",
            "The corridor bends. RIVETS cannot guess; you must tell it when to turn.",
            "moveRight, moveDown",
            "moveRight\nmoveRight\nmoveRight\nmoveDown\nmoveDown",
            "Translate the turn into exact movement commands."),
        data("Cardinal Control",
            "All four directions are now available. The path ahead changes course. Stay sharp.",
            "moveRight, moveDown, moveLeft, moveUp",
            "moveRight\nmoveDown\nmoveRight",
            "Use direction changes to keep RIVETS on the safe path."),
        data("Endurance",
            "A long straight path. RIVETS does not tire. Do you?",
            "moveRight, moveDown, moveLeft, moveUp",
            "moveRight\nmoveRight\nmoveRight\n...",
            "Reach the goal with a precise sequential program."),
        data("Mixed Path",
            "The chamber now asks for planning, not just counting.",
            "moveRight, moveDown, moveLeft, moveUp",
            "moveRight\nmoveRight\nmoveDown\nmoveRight",
            "Break the route into ordered movement phases."),
        data("Detour",
            "The direct route is blocked. RIVETS needs a programmer who can route around trouble.",
            "moveRight, moveDown, moveLeft, moveUp",
            "moveDown\nmoveRight\nmoveRight\nmoveUp",
            "Navigate around obstacles without losing the goal."),
        data("Offset Corridor",
            "The path steps sideways through the chamber. Plan the whole route before you run.",
            "moveRight, moveDown, moveLeft, moveUp",
            "moveRight\nmoveRight\nmoveDown\nmoveDown",
            "Complete the final sequential-control test."),
        data("Pattern Recognition",
            "You have walked this corridor before. A skilled programmer does not repeat themselves; they compress.",
            "moveRight, moveDown, moveLeft, moveUp, repeat(N) { ... }",
            "repeat(8) {\n  moveRight\n}",
            "Use repeat to express repeated movement cleanly."),
        data("Step and Repeat",
            "Not every part of a route repeats. Choose the part that does.",
            "moveRight, moveDown, moveLeft, moveUp, repeat(N) { ... }",
            "repeat(5) {\n  moveRight\n}\nmoveDown",
            "Loop one segment, then finish the route sequentially."),
        data("Two Segments",
            "Two repeated stretches are separated by a turn. Structure can appear more than once.",
            "moveRight, moveDown, moveLeft, moveUp, repeat(N) { ... }",
            "repeat(3) {\n  moveRight\n}\nmoveDown\nrepeat(3) {\n  moveRight\n}",
            "Use loops for both repeated segments."),
        data("Column Descent",
            "The pattern now has width and depth. RIVETS is ready for nested structure.",
            "move commands, repeat(N) { ... }, nested repeat blocks",
            "repeat(3) {\n  repeat(3) {\n    moveRight\n  }\n  moveDown\n}",
            "Recognise the repeated row pattern."),
        data("Stagger Grid",
            "Each row shifts the structure. The pattern is real, but it must be read carefully.",
            "move commands, repeat(N) { ... }, nested repeat blocks",
            "repeat(3) {\n  repeat(3) {\n    moveRight\n  }\n  moveDown\n}",
            "Use structure while accounting for the offset."),
        data("Broken Symmetry",
            "This chamber almost repeats perfectly. Almost is the test.",
            "move commands, repeat(N) { ... }, hybrid loop and sequential logic",
            "repeat(2) {\n  moveRight\n  moveDown\n}\nmoveRight",
            "Combine loops with exact standalone moves."),
        data("Wide Traversal",
            "The route wraps across the chamber. Large structures reward calm decomposition.",
            "move commands, repeat(N) { ... }, multi-segment loops",
            "repeat(5) {\n  moveRight\n}\nmoveDown\nrepeat(5) {\n  moveLeft\n}",
            "Represent long route segments efficiently."),
        data("Final Chamber",
            "The full training cycle converges here. RIVETS will be classified by the quality of your control.",
            "all movement commands, repeat(N) { ... }, nested repeat blocks",
            "repeat(3) {\n  repeat(6) {\n    moveRight\n  }\n  moveDown\n}",
            "Complete the synthesis chamber and receive the final RIVETS classification.")
    };

    private static final LevelIntroData[] CACHE = new LevelIntroData[FALLBACK_DATA.length];

    private LevelIntroRegistry() {
    }

    public static LevelIntroData forLevelNumber(int levelNumber) {
        int index = Math.max(1, Math.min(FALLBACK_DATA.length, levelNumber)) - 1;
        if (CACHE[index] == null) {
            CACHE[index] = loadLevelData(index + 1, FALLBACK_DATA[index]);
        }
        return CACHE[index];
    }

    private static LevelIntroData loadLevelData(int levelNumber, LevelIntroData fallback) {
        try {
            String json = readJson(levelNumber);
            return new LevelIntroData(
                stringField(json, "chamberName", fallback.chamberName),
                stringField(json, "narrative", fallback.narrative),
                stringField(json, "commands", fallback.commands),
                stringField(json, "example", fallback.example),
                stringField(json, "objective", fallback.objective));
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private static String readJson(int levelNumber) {
        String fileName = String.format("level%02d.json", levelNumber);
        Path path = Paths.get(INTRO_DIR, fileName);
        if (Files.exists(path)) {
            try {
                return Files.readString(path, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read " + path, e);
            }
        }

        String resourceName = "/" + INTRO_DIR + "/" + fileName;
        try (InputStream in = LevelIntroRegistry.class.getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Missing intro JSON " + resourceName);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + resourceName, e);
        }
    }

    private static String stringField(String json, String key, String fallback) {
        int keyStart = findKey(json, key);
        if (keyStart < 0) return fallback;

        int colon = json.indexOf(':', keyStart);
        if (colon < 0) return fallback;

        int valueStart = skipWhitespace(json, colon + 1);
        if (valueStart >= json.length() || json.charAt(valueStart) != '"') return fallback;

        return readStringValue(json, valueStart);
    }

    private static int findKey(String json, String key) {
        String quotedKey = "\"" + key + "\"";
        return json.indexOf(quotedKey);
    }

    private static int skipWhitespace(String text, int index) {
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }

    private static String readStringValue(String json, int quoteIndex) {
        StringBuilder value = new StringBuilder();
        boolean escaping = false;
        for (int i = quoteIndex + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaping) {
                value.append(unescape(ch));
                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            } else if (ch == '"') {
                return value.toString();
            } else {
                value.append(ch);
            }
        }
        throw new IllegalArgumentException("Unterminated JSON string");
    }

    private static char unescape(char ch) {
        switch (ch) {
            case 'n': return '\n';
            case 'r': return '\r';
            case 't': return '\t';
            case '"': return '"';
            case '\\': return '\\';
            case '/': return '/';
            default: return ch;
        }
    }

    private static LevelIntroData data(String name, String narrative, String commands, String example, String objective) {
        return new LevelIntroData(name, narrative, commands, example, objective);
    }
}
