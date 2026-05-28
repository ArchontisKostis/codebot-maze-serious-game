import java.util.Arrays;

public final class OnboardRegistry {

    private OnboardRegistry() {}

    public static OnboardFlow forLevel(int levelNumber) {
        if (levelNumber == 1) return level1Flow();
        return null;
    }

    private static OnboardFlow level1Flow() {
        return new OnboardFlow(Arrays.asList(
            // 1 — Welcome
            step(
                "Welcome to CodeBot — Level 1",
                new String[]{
                    "Welcome to the first chamber.",
                    "In this tutorial you'll learn the layout of",
                    "the screen and your first command."
                },
                OnboardStep.SpotlightRegion.NONE,
                OnboardStep.CardSide.CENTER
            ),
            // 2 — Screen overview: game area
            step(
                "Game screen overview",
                new String[]{
                    "This is your workspace.",
                    "Top left: the grid with RIVETS,",
                    "obstacles and the goal."
                },
                OnboardStep.SpotlightRegion.GAME_AREA,
                OnboardStep.CardSide.RIGHT
            ),
            // 3 — Screen overview: code editor
            step(
                "Code editor",
                new String[]{
                    "Right side: the code editor.",
                    "Type commands here to control the robot."
                },
                OnboardStep.SpotlightRegion.CODE_EDITOR,
                OnboardStep.CardSide.LEFT
            ),
            // 4 — Screen overview: controls
            step(
                "Controls panel",
                new String[]{
                    "When done, use the controls panel to run",
                    "your program.",
                    "The reset button returns the robot to its",
                    "starting position."
                },
                OnboardStep.SpotlightRegion.CONTROLS,
                OnboardStep.CardSide.LEFT
            ),
            // 5 — Screen overview: output
            step(
                "Output",
                new String[]{
                    "Bottom right: output shows messages and hints.",
                    "Always look at it — it helps!"
                },
                OnboardStep.SpotlightRegion.TERMINAL,
                OnboardStep.CardSide.LEFT
            ),
            // 6 — How to pass
            step(
                "How to pass this level",
                new String[]{
                    "Your goal is the green tile in the game area.",
                    "Write commands so the robot reaches the goal",
                    "without hitting obstacles.",
                    "Commands run top to bottom; reach the goal",
                    "to pass. If stuck, press Reset and try again."
                },
                OnboardStep.SpotlightRegion.GAME_AREA,
                OnboardStep.CardSide.RIGHT
            ),
            // 7 — First command: move
            step(
                "First command — move",
                new String[]{
                    "The move command moves the robot one tile.",
                    "Examples: move up, move right,",
                    "          move down, move left.",
                    "Try: move right / move right / move up",
                    "Use Step to run line-by-line while learning."
                },
                OnboardStep.SpotlightRegion.CODE_EDITOR,
                OnboardStep.CardSide.LEFT
            ),
            // 8 — Try it now
            step(
                "Try it now",
                new String[]{
                    "Write a short sequence using move",
                    "to reach the green goal.",
                    "Press Run to execute or Step to advance",
                    "one command at a time.",
                    "You can Reset anytime to try a new solution."
                },
                OnboardStep.SpotlightRegion.CONTROLS,
                OnboardStep.CardSide.LEFT
            )
        ));
    }

    private static OnboardStep step(String title, String[] lines,
            OnboardStep.SpotlightRegion spotlight, OnboardStep.CardSide cardSide) {
        return new OnboardStep(title, lines, spotlight, cardSide);
    }
}
