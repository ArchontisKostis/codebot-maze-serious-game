import java.util.List;

/**
 * AbstractionScorer
 *
 * Scores the player on code quality by walking the AST:
 *   - 25 pts per {@link RepeatStatement} found (at any depth), capped at 100.
 *   - Additional 25 pts × deepest nesting level (0 = top level, 1 = nested, …).
 *
 * Score examples:
 *   No repeats                        →   0
 *   1 flat repeat                     →  25
 *   2 flat repeats                    →  50
 *   1 nested pair (repeat in repeat)  →  50  (2 nodes × 25 + depth 1 × 25 = 75, wait…)
 *
 * Exact formula: min(100, repeatCount × 25 + maxDepth × 25)
 *   1 repeat, depth 0  →  25 + 0  = 25
 *   2 repeats, depth 0 →  50 + 0  = 50
 *   2 repeats, depth 1 →  50 + 25 = 75
 *   3 repeats, depth 1 →  75 + 25 = 100 (capped)
 */
public class AbstractionScorer implements LevelScorer {

    @Override
    public int score(Program program, int totalCoins, int coinsCollected, int attempts) {
        int repeatCount = countRepeats(program.statements);
        if (repeatCount == 0) return 0;
        int maxDepth = maxRepeatDepth(program.statements, 0);
        return Math.min(100, repeatCount * 25 + maxDepth * 25);
    }

    private int countRepeats(List<Statement> stmts) {
        int count = 0;
        for (Statement s : stmts) {
            if (s instanceof RepeatStatement) {
                RepeatStatement r = (RepeatStatement) s;
                count += 1 + countRepeats(r.body);
            }
        }
        return count;
    }

    /**
     * Returns the depth of the deepest repeat in the tree, or -1 if none.
     * Depth 0 means the repeat appears directly inside the program (top level).
     */
    private int maxRepeatDepth(List<Statement> stmts, int depth) {
        int max = -1;
        for (Statement s : stmts) {
            if (s instanceof RepeatStatement) {
                RepeatStatement r = (RepeatStatement) s;
                max = Math.max(max, depth);
                max = Math.max(max, maxRepeatDepth(r.body, depth + 1));
            }
        }
        return max;
    }
}
