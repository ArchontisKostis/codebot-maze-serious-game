import java.util.ArrayList;
import java.util.List;

/**
 * Parser
 *
 * Recursive-descent parser. Consumes a token list produced by Lexer
 * and produces a Program AST.
 *
 * Grammar (EBNF):
 *   program   ::= statement* EOF
 *   statement ::= move | repeat
 *   move      ::= MOVE_UP | MOVE_DOWN | MOVE_LEFT | MOVE_RIGHT
 *   repeat    ::= REPEAT LPAREN NUMBER RPAREN statement* END
 *
 * On malformed input a ParseError is thrown with the offending line number.
 */
public class Parser {

    // ── Errors ────────────────────────────────────────────────────────────────

    public static class ParseError extends RuntimeException {
        public final int line;

        public ParseError(String message, int line) {
            super("Line " + line + ": " + message);
            this.line = line;
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private final List<Lexer.Token> tokens;
    private int pos = 0;

    public Parser(List<Lexer.Token> tokens) {
        this.tokens = tokens;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public Program parse() {
        List<Statement> stmts = parseBlock();
        if (peek().type != Lexer.TokenType.EOF) {
            Lexer.Token t = peek();
            throw new ParseError("Unexpected '" + t.raw + "'", t.line);
        }
        return new Program(stmts);
    }

    // ── Grammar rules ─────────────────────────────────────────────────────────

    /**
     * Parses a sequence of statements until EOF or 'end' is reached.
     * 'end' itself is NOT consumed here — the caller (parseRepeat) consumes it.
     */
    private List<Statement> parseBlock() {
        List<Statement> stmts = new ArrayList<>();
        while (peek().type != Lexer.TokenType.EOF
            && peek().type != Lexer.TokenType.END) {
            stmts.add(parseStatement());
        }
        return stmts;
    }

    private Statement parseStatement() {
        Lexer.Token t = peek();
        switch (t.type) {
            case MOVE_UP:    advance(); return new MoveStatement(MoveStatement.Direction.UP);
            case MOVE_DOWN:  advance(); return new MoveStatement(MoveStatement.Direction.DOWN);
            case MOVE_LEFT:  advance(); return new MoveStatement(MoveStatement.Direction.LEFT);
            case MOVE_RIGHT: advance(); return new MoveStatement(MoveStatement.Direction.RIGHT);
            case REPEAT:     return parseRepeat();
            default:
                throw new ParseError("Expected a statement, got '" + t.raw + "'", t.line);
        }
    }

    /**
     * repeat ::= REPEAT LPAREN NUMBER RPAREN statement* END
     */
    private Statement parseRepeat() {
        expect(Lexer.TokenType.REPEAT);
        expect(Lexer.TokenType.LPAREN);

        Lexer.Token numTok = expect(Lexer.TokenType.NUMBER);
        int times = Integer.parseInt(numTok.raw);
        if (times < 1 || times > 100) {
            throw new ParseError("repeat count must be between 1 and 100", numTok.line);
        }

        expect(Lexer.TokenType.RPAREN);

        List<Statement> body = parseBlock();

        if (peek().type != Lexer.TokenType.END) {
            throw new ParseError(
                "Expected 'end' to close repeat(" + times + "), got '" + peek().raw + "'",
                peek().line
            );
        }
        expect(Lexer.TokenType.END);

        return new RepeatStatement(times, body);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Lexer.Token peek() {
        return tokens.get(pos);
    }

    private Lexer.Token advance() {
        return tokens.get(pos++);
    }

    private Lexer.Token expect(Lexer.TokenType type) {
        Lexer.Token t = peek();
        if (t.type != type) {
            throw new ParseError(
                "Expected " + type + " but got '" + t.raw + "'",
                t.line
            );
        }
        return advance();
    }
}
