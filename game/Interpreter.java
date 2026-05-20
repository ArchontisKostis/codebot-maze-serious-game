import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Interpreter
 *
 * Stack-based execution engine designed for Greenfoot's frame-by-frame act() model.
 *
 * Because Greenfoot's game loop is flat (one act() call per frame), we cannot use
 * ordinary recursion to walk nested loops — the whole traversal would happen in a
 * single frame with no animation. Instead, we maintain an explicit call stack of
 * Frames. Each act() call advances the interpreter exactly ONE step (one move).
 *
 * How to use:
 *   interpreter.load(program);            // called once on RUN
 *   // in act():
 *   MoveStatement move = interpreter.next();
 *   if (move == null) { // done }
 *   else { robot.executeMove(move); }
 *
 * Loops are never "expanded" — they stay as RepeatStatement nodes in the AST.
 * A loop pushes a new Frame onto the stack; the Frame tracks which iteration
 * we are on. When the body is exhausted, the Frame resets for the next iteration
 * (or pops itself when all iterations are done).
 */
public class Interpreter {

    // ── Frame ─────────────────────────────────────────────────────────────────

    private static class Frame {
        final List<Statement> stmts;
        int index;              // next statement to execute in this block
        int iteration;          // current loop iteration (0-based)
        final int totalIter;    // total iterations (always 1 for top-level frame)

        Frame(List<Statement> stmts, int totalIter) {
            this.stmts     = stmts;
            this.index     = 0;
            this.iteration = 0;
            this.totalIter = totalIter;
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────

    // Stack top = currently executing block.
    private final Deque<Frame> stack = new ArrayDeque<>();
    private boolean running = false;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Load a program and prepare for execution.
     * Call this once when the user presses RUN.
     */
    public void load(Program program) {
        stack.clear();
        running = !program.isEmpty();
        if (running) {
            stack.push(new Frame(program.statements, 1));
        }
    }

    /** True while there are still statements left to execute. */
    public boolean isRunning() {
        return running;
    }

    /** Stop execution immediately (e.g. goal reached mid-program). */
    public void halt() {
        running = false;
        stack.clear();
    }

    /**
     * Advance one execution step.
     *
     * Returns the next MoveStatement that the robot should physically execute,
     * or null if the program has finished. Internally skips over RepeatStatements
     * by pushing new Frames — the caller never sees compound nodes.
     *
     * Call this once per act() frame.
     */
    public MoveStatement next() {
        while (!stack.isEmpty()) {
            Frame frame = stack.peek();

            // ── Current block exhausted ──────────────────────────────────────
            if (frame.index >= frame.stmts.size()) {
                frame.iteration++;
                if (frame.iteration < frame.totalIter) {
                    // Loop: reset block and go again
                    frame.index = 0;
                } else {
                    // Done with this frame (loop finished or top-level end)
                    stack.pop();
                }
                continue;
            }

            // ── Consume next statement ───────────────────────────────────────
            Statement stmt = frame.stmts.get(frame.index++);

            if (stmt instanceof MoveStatement) {
                // Leaf node — return it to the caller for physical execution
                return (MoveStatement) stmt;

            } else if (stmt instanceof RepeatStatement) {
                RepeatStatement repeat = (RepeatStatement) stmt;
                if (!repeat.body.isEmpty()) {
                    stack.push(new Frame(repeat.body, repeat.times));
                }
                // Don't return — loop inward to get the first move of the body
            }
        }

        // Stack empty → execution complete
        running = false;
        return null;
    }
}
