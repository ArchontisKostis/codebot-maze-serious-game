import java.util.Collections;
import java.util.List;

/**
 * RepeatStatement
 *
 * AST compound node representing a counted loop.
 * The body is an immutable list of child statements — loops can be nested.
 *
 * Grammar:
 *   repeat ::= "repeat" "(" NUMBER ")" statement* "end"
 */
public class RepeatStatement implements Statement {

    public final int times;
    public final List<Statement> body;

    public RepeatStatement(int times, List<Statement> body) {
        this.times = times;
        this.body  = Collections.unmodifiableList(body);
    }
}
