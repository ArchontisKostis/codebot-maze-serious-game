public final class LevelProgressionController {

    private final MyWorldSessionState sessionState;
    private final TerminalManager terminalManager;

    public LevelProgressionController(MyWorldSessionState sessionState, TerminalManager terminalManager) {
        this.sessionState = sessionState;
        this.terminalManager = terminalManager;
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
        LevelManager.recordCurrentLevelStars(stars);
        terminalManager.log("~ # *** LEVEL COMPLETE! ***");
        terminalManager.log("~ # " + starString(stars) + "  (" + LevelManager.getTotalStars() + "/" + LevelManager.getMaxStars() + " total)");
        sessionState.setGoalAdvanceCountdown(60); // ~1 s at default Greenfoot speed
    }

    private int calculateStars(Program program) {
        if (LevelManager.getCurrentLevelNumber() <= 7) {
            return 3;
        }

        int abstraction = new AbstractionScorer().score(
            program,
            sessionState.getTotalCoins(),
            sessionState.getCoinsCollected(),
            sessionState.getAttempts());
        if (abstraction >= 75) return 3;
        if (abstraction >= 25) return 2;
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