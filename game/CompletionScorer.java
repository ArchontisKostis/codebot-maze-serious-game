/**
 * CompletionScorer
 *
 * Scores the player on execution quality:
 *   - Coin collection (50 pts): proportional to coins collected vs. available.
 *     Levels with no coins award the full 50 automatically.
 *   - Attempt efficiency (50 pts): 50 on first try, -10 per additional attempt (min 0).
 */
public class CompletionScorer implements LevelScorer {

    @Override
    public int score(Program program, int totalCoins, int coinsCollected, int attempts) {
        int coinScore = (totalCoins == 0) ? 50 : coinsCollected * 50 / totalCoins;
        int atmpScore = (attempts == 1)   ? 50 : Math.max(0, 50 - (attempts - 1) * 10);
        return coinScore + atmpScore;
    }
}
