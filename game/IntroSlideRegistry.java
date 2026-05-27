import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IntroSlideRegistry {
    private static final String SLIDES_PATH = "intro/slides.json";
    private static final String SLIDES_RESOURCE = "/intro/slides.json";

    private static List<IntroSlideData> cache;

    private IntroSlideRegistry() {
    }

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
                    String obj = json.substring(objectStart, i + 1);
                    result.add(parseObject(obj));
                    objectStart = -1;
                }
            }
        }
        return result;
    }

    private static IntroSlideData parseObject(String obj) {
        String image = stringField(obj, "image");
        String text = stringField(obj, "text");
        return new IntroSlideData(image, text);
    }

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
}
