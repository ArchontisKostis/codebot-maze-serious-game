import greenfoot.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lenient syntax colouring for the robot DSL shown in {@link CodeEditor}.
 *
 * <p>Spans are computed per line so incomplete programs still highlight while typing.
 * Keyword recognition uses {@link Lexer#keywordTypeForLowercase(String)} — the same
 * mapping as the strict lexer — so new keywords only need updating in {@link Lexer}.
 */
public final class CodeSyntaxHighlighter {

    /** Semantic bucket for a contiguous slice of text on one editor line. */
    public enum Style {
        /** Spaces, tabs, newlines (within-line gaps only). */
        WHITESPACE,
        /** {@code moveUp}, {@code moveDown}, {@code moveLeft}, {@code moveRight}. */
        MOVE_KEYWORD,
        /** {@code repeat}, {@code end}. */
        CONTROL_KEYWORD,
        NUMBER,
        PAREN,
        /** Letters that are not a known keyword. */
        UNKNOWN_WORD,
        /** Any other single character (illegal in the DSL). */
        INVALID
    }

    /** Half-open interval {@code [start, end)} relative to the line string passed to {@link #spansForLine}. */
    public static final class Span {
        public final int   start;
        public final int   end;
        public final Style style;

        public Span(int start, int end, Style style) {
            this.start = start;
            this.end   = end;
            this.style = style;
        }
    }

    private CodeSyntaxHighlighter() {}

    /**
     * Theme colours for {@link Style}. Adjust here only — keeps the editor UI consistent.
     */
    public static Color colorFor(Style style) {
        switch (style) {
            case WHITESPACE:
                return new Color(140, 140, 170);
            case MOVE_KEYWORD:
                return new Color(130, 200, 255);
            case CONTROL_KEYWORD:
                return new Color(255, 180, 120);
            case NUMBER:
                return new Color(255, 220, 120);
            case PAREN:
                return new Color(200, 160, 255);
            case UNKNOWN_WORD:
                return new Color(230, 140, 140);
            case INVALID:
                return new Color(255, 100, 100);
            default:
                return new Color(210, 210, 250);
        }
    }

    /**
     * Partition {@code line} into styled spans. Never throws; unknown / illegal input still yields spans.
     */
    public static List<Span> spansForLine(String line) {
        if (line == null || line.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<Span> out = new ArrayList<>();
        int pos = 0;
        final int n = line.length();

        while (pos < n) {
            char c = line.charAt(pos);

            if (Character.isWhitespace(c)) {
                int start = pos;
                while (pos < n && Character.isWhitespace(line.charAt(pos))) {
                    pos++;
                }
                out.add(new Span(start, pos, Style.WHITESPACE));
                continue;
            }

            if (c == '(' || c == ')') {
                out.add(new Span(pos, pos + 1, Style.PAREN));
                pos++;
                continue;
            }

            if (Character.isDigit(c)) {
                int start = pos;
                while (pos < n && Character.isDigit(line.charAt(pos))) {
                    pos++;
                }
                out.add(new Span(start, pos, Style.NUMBER));
                continue;
            }

            if (Character.isLetter(c)) {
                int start = pos;
                while (pos < n && Character.isLetter(line.charAt(pos))) {
                    pos++;
                }
                String word = line.substring(start, pos);
                String lower = word.toLowerCase();
                Lexer.TokenType kw = Lexer.keywordTypeForLowercase(lower);
                Style st = kw == null
                    ? Style.UNKNOWN_WORD
                    : styleForKeyword(kw);
                out.add(new Span(start, pos, st));
                continue;
            }

            out.add(new Span(pos, pos + 1, Style.INVALID));
            pos++;
        }

        return out;
    }

    private static Style styleForKeyword(Lexer.TokenType t) {
        switch (t) {
            case MOVE_UP:
            case MOVE_DOWN:
            case MOVE_LEFT:
            case MOVE_RIGHT:
                return Style.MOVE_KEYWORD;
            case REPEAT:
            case END:
                return Style.CONTROL_KEYWORD;
            default:
                return Style.UNKNOWN_WORD;
        }
    }
}
