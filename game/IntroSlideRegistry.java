import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IntroSlideRegistry {
    private static final String SLIDES_PATH     = "intro/slides.json";
    private static final String SLIDES_RESOURCE = "/intro/slides.json";

    private static List<IntroSlideData> cache;

    private IntroSlideRegistry() {}

    public static List<IntroSlideData> load() {
        if (cache == null) {
            cache = Collections.unmodifiableList(parseSlides(readJson()));
        }
        return cache;
    }

    private static String readJson() {
        if (Files.exists(Paths.get(SLIDES_PATH))) {
            try {
                return Files.readString(Paths.get(SLIDES_PATH), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read " + SLIDES_PATH, e);
            }
        }
        try (InputStream in = IntroSlideRegistry.class.getResourceAsStream(SLIDES_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing intro slides JSON: " + SLIDES_RESOURCE);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + SLIDES_RESOURCE, e);
        }
    }

    private static List<IntroSlideData> parseSlides(String json) {
        List<IntroSlideData> result = new ArrayList<>();
        int depth = 0;
        int objectStart = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) objectStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objectStart >= 0) {
                    result.add(parseObject(json.substring(objectStart, i + 1)));
                    objectStart = -1;
                }
            }
        }
        return result;
    }

    private static IntroSlideData parseObject(String obj) {
        // Try "images" array first; fall back to single "image" string
        String[] images = stringArrayField(obj, "images");
        if (images == null || images.length == 0) {
            String single = stringField(obj, "image");
            images = new String[]{ single };
        }
        String text = stringField(obj, "text");
        return new IntroSlideData(images, text);
    }

    // ── JSON helpers ──────────────────────────────────────────────────────────

    private static String stringField(String json, String key) {
        String quotedKey = "\"" + key + "\"";
        int keyStart = json.indexOf(quotedKey);
        if (keyStart < 0) return "";
        int colon = json.indexOf(':', keyStart);
        if (colon < 0) return "";
        int valueStart = skipWhitespace(json, colon + 1);
        if (valueStart >= json.length() || json.charAt(valueStart) != '"') return "";
        return readStringValue(json, valueStart);
    }

    private static String[] stringArrayField(String json, String key) {
        String quotedKey = "\"" + key + "\"";
        int keyStart = json.indexOf(quotedKey);
        if (keyStart < 0) return null;
        int colon = json.indexOf(':', keyStart);
        if (colon < 0) return null;
        int pos = skipWhitespace(json, colon + 1);
        if (pos >= json.length() || json.charAt(pos) != '[') return null;

        List<String> values = new ArrayList<>();
        pos++;
        while (pos < json.length()) {
            pos = skipWhitespace(json, pos);
            if (pos >= json.length()) break;
            char c = json.charAt(pos);
            if (c == ']') break;
            if (c == '"') {
                values.add(readStringValue(json, pos));
                pos = findEndOfString(json, pos + 1) + 1;
            } else if (c == ',') {
                pos++;
            } else {
                pos++;
            }
        }
        return values.toArray(new String[0]);
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
        throw new IllegalArgumentException("Unterminated JSON string in intro slides");
    }

    // Returns the index of the closing quote, starting the scan from afterOpenQuote.
    private static int findEndOfString(String json, int afterOpenQuote) {
        boolean escaping = false;
        for (int i = afterOpenQuote; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaping) { escaping = false; }
            else if (ch == '\\') { escaping = true; }
            else if (ch == '"') { return i; }
        }
        return json.length() - 1;
    }

    private static char unescape(char ch) {
        switch (ch) {
            case 'n':  return '\n';
            case 'r':  return '\r';
            case 't':  return '\t';
            case '"':  return '"';
            case '\\': return '\\';
            case '/':  return '/';
            default:   return ch;
        }
    }
}
