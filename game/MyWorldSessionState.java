public final class MyWorldSessionState {

    private int attempts;
    private int goalAdvanceCountdown;
    private int totalCoins;
    private int coinsCollected;
    private Program lastProgram;
    private boolean introOverlayActive;
    private boolean programEndOverlayActive;

    public MyWorldSessionState() {
        this.goalAdvanceCountdown = -1;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        attempts++;
    }

    public int getGoalAdvanceCountdown() {
        return goalAdvanceCountdown;
    }

    public void setGoalAdvanceCountdown(int goalAdvanceCountdown) {
        this.goalAdvanceCountdown = goalAdvanceCountdown;
    }

    public boolean hasGoalAdvanceCountdown() {
        return goalAdvanceCountdown >= 0;
    }

    public void decrementGoalAdvanceCountdown() {
        goalAdvanceCountdown--;
    }

    public int getTotalCoins() {
        return totalCoins;
    }

    public void setTotalCoins(int totalCoins) {
        this.totalCoins = totalCoins;
    }

    public int getCoinsCollected() {
        return coinsCollected;
    }

    public void setCoinsCollected(int coinsCollected) {
        this.coinsCollected = coinsCollected;
    }

    public void incrementCoinsCollected() {
        coinsCollected++;
    }

    public Program getLastProgram() {
        return lastProgram;
    }

    public void setLastProgram(Program lastProgram) {
        this.lastProgram = lastProgram;
    }

    public boolean isIntroOverlayActive() {
        return introOverlayActive;
    }

    public void setIntroOverlayActive(boolean introOverlayActive) {
        this.introOverlayActive = introOverlayActive;
    }

    public boolean isProgramEndOverlayActive() {
        return programEndOverlayActive;
    }

    public void setProgramEndOverlayActive(boolean programEndOverlayActive) {
        this.programEndOverlayActive = programEndOverlayActive;
    }
}