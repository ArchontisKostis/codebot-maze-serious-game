import java.util.Collections;
import java.util.List;

/**
 * Program
 *
 * Root AST node. Holds the top-level list of statements that make up
 * a complete robot program. Produced by Parser, consumed by Interpreter.
 */
public class Program {

    public final List<Statement> statements;

    public Program(List<Statement> statements) {
        this.statements = Collections.unmodifiableList(statements);
    }

    public boolean isEmpty() {
        return statements.isEmpty();
    }
}
