import java.util.ArrayList;
import java.util.List;

/**
 * Lexer
 *
 * Converts raw source text into a flat list of tokens.
 * Keyword matching is case-insensitive, so "moveUp", "MOVEUP", "moveup" all work.
 * Whitespace (spaces, tabs, newlines) is silently skipped between tokens.
 *
 * Supported tokens:
 *   Letters  → MOVE_UP / MOVE_DOWN / MOVE_LEFT / MOVE_RIGHT / REPEAT / END
 *   Digits   → NUMBER
 *   (        → LPAREN
 *   )        → RPAREN
 *
 * On unrecognised input a LexError is thrown with the offending line number.
 */
public class Lexer {

    // ── Token types ──────────────────────────────────────────────────────────

    public enum TokenType {
        MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT,
        REPEAT, LPAREN, RPAREN, NUMBER, END,
        EOF
    }

    // ── Token ─────────────────────────────────────────────────────────────────

    public static class Token {
        public final TokenType type;
        public final String    raw;   // original text from source
        public final int       line;

        public Token(TokenType type, String raw, int line) {
            this.type = type;
            this.raw  = raw;
            this.line = line;
        }

        @Override
        public String toString() {
            return type + "(\"" + raw + "\" L" + line + ")";
        }
    }

    // ── Errors ────────────────────────────────────────────────────────────────

    public static class LexError extends RuntimeException {
        public final int line;

        public LexError(String message, int line) {
            super("Line " + line + ": " + message);
            this.line = line;
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private final String src;
    private int pos  = 0;
    private int line = 1;

    public Lexer(String src) {
        this.src = src;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < src.length()) {
            skipWhitespace();
            if (pos >= src.length()) break;

            char c = src.charAt(pos);

            if (c == '(') {
                tokens.add(new Token(TokenType.LPAREN, "(", line));
                pos++;
            } else if (c == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")", line));
                pos++;
            } else if (Character.isDigit(c)) {
                tokens.add(readNumber());
            } else if (Character.isLetter(c)) {
                tokens.add(readKeyword());
            } else {
                throw new LexError("Unexpected character: '" + c + "'", line);
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void skipWhitespace() {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == '\n') { line++; pos++; }
            else if (Character.isWhitespace(c)) { pos++; }
            else break;
        }
    }

    private Token readNumber() {
        int start     = pos;
        int startLine = line;
        while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
        return new Token(TokenType.NUMBER, src.substring(start, pos), startLine);
    }

    private Token readKeyword() {
        int start     = pos;
        int startLine = line;
        while (pos < src.length() && Character.isLetter(src.charAt(pos))) pos++;

        String raw   = src.substring(start, pos);
        String lower = raw.toLowerCase();

        TokenType kw = keywordTypeForLowercase(lower);
        if (kw != null) {
            return new Token(kw, raw, startLine);
        }
        throw new LexError("Unknown keyword: '" + raw + "'", startLine);
    }

    /**
     * Looks up a keyword after normalising to lowercase ({@code moveUp} → {@code moveup}).
     * Used by the lexer and {@link CodeSyntaxHighlighter} so keywords stay defined in one place.
     *
     * @return the token type, or {@code null} if {@code lowerCaseWord} is not a keyword
     */
    public static TokenType keywordTypeForLowercase(String lowerCaseWord) {
        switch (lowerCaseWord) {
            case "moveup":    return TokenType.MOVE_UP;
            case "movedown":  return TokenType.MOVE_DOWN;
            case "moveleft":  return TokenType.MOVE_LEFT;
            case "moveright": return TokenType.MOVE_RIGHT;
            case "repeat":    return TokenType.REPEAT;
            case "end":       return TokenType.END;
            default:
                return null;
        }
    }
}
