public final class LevelProgressionController {

    private final MyWorldSessionState sessionState;
    private final TerminalManager terminalManager;

    private ScorerKind scorerKind = ScorerKind.COMPLETION;
    private int twoStarThreshold = LevelDefinition.DEFAULT_TWO_STAR;
    private int threeStarThreshold = LevelDefinition.DEFAULT_THREE_STAR;

    /** Custom (Free Play) levels score for the overlay but never touch campaign progress. */
    private boolean recordToCampaign = true;

    public LevelProgressionController(MyWorldSessionState sessionState, TerminalManager terminalManager) {
        this.sessionState = sessionState;
        this.terminalManager = terminalManager;
    }

    /** Configures which scorer and thresholds decide this level's stars (read from the level document). */
    public void setScoring(ScorerKind scorerKind, int twoStarThreshold, int threeStarThreshold) {
        this.scorerKind = scorerKind;
        this.twoStarThreshold = twoStarThreshold;
        this.threeStarThreshold = threeStarThreshold;
    }

    /** When false (custom/Free Play levels), completion does not write to {@link LevelManager} campaign state. */
    public void setRecordToCampaign(boolean recordToCampaign) {
        this.recordToCampaign = recordToCampaign;
    }

    public boolean tickAdvanceCountdown(boolean interactionLocked) {
        if (interactionLocked) return false;
        if (!sessionState.hasGoalAdvanceCountdown()) return false;

        if (sessionState.getGoalAdvanceCountdown() > 0) {
            sessionState.decrementGoalAdvanceCountdown();
            return false;
        }

        sessionState.setGoalAdvanceCountdown(-1);
        return true;
    }

    public void onGoalReached() {
        if (sessionState.hasGoalAdvanceCountdown()) return;

        Program lastProgram = sessionState.getLastProgram();
        int stars = (lastProgram == null) ? 3 : calculateStars(lastProgram);
        sessionState.setLastStars(stars);
        if (recordToCampaign) {
            LevelManager.recordCurrentLevelStars(stars);
        }
        terminalManager.log("~ # *** LEVEL COMPLETE! ***");
        terminalManager.log("~ # " + starString(stars));
        sessionState.setGoalAdvanceCountdown(60); // ~1 s at default Greenfoot speed
    }

    private int calculateStars(Program program) {
        if (scorerKind == ScorerKind.COMPLETION) {
            return 3;
        }

        int abstraction = new AbstractionScorer().score(
            program,
            sessionState.getTotalCoins(),
            sessionState.getCoinsCollected(),
            sessionState.getAttempts());
        if (abstraction >= threeStarThreshold) return 3;
        if (abstraction >= twoStarThreshold) return 2;
        return 1;
    }

    private String starString(int stars) {
        switch (stars) {
            case 3:  return "★★★";
            case 2:  return "★★☆";
            case 1:  return "★☆☆";
            default: return "☆☆☆";
        }
    }
}