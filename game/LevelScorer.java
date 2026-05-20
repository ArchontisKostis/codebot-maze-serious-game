/**
 * LevelScorer
 *
 * Computes a 0–100 score for one level completion.
 * Two concrete implementations cover the two grading axes:
 * {@link CompletionScorer} (coins + attempts) and
 * {@link AbstractionScorer} (repeat usage + nesting depth).
 */
public interface LevelScorer {

    /**
     * @param program        the parsed program used to reach the goal
     * @param totalCoins     coins present in the level at start
     * @param coinsCollected coins the robot actually picked up
     * @param attempts       total run attempts so far (1 = first try)
     * @return score in [0, 100]
     */
    int score(Program program, int totalCoins, int coinsCollected, int attempts);
}
