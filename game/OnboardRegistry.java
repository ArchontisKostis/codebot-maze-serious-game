import java.util.Arrays;

public final class OnboardRegistry {

    private OnboardRegistry() {}

    public static OnboardFlow forLevel(int levelNumber) {
        switch (levelNumber) {
            case 1:  return level1Flow();
            case 2:  return level2Flow();
            case 3:  return level3Flow();
            case 8:  return level8Flow();
            case 11: return level11Flow();
            default: return null;   // levels with "no script" get no onboarding
        }
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

    // Level 2 — unlocks moveDown. Source: story/between-levels/level-2.md
    private static OnboardFlow level2Flow() {
        return new OnboardFlow(Arrays.asList(
            step(
                "New Command Unlocked!",
                new String[]{
                    "You unlocked a new command!",
                    "moveDown",
                    "Moves RIVETS one tile downward.",
                    "You can now combine moveRight and moveDown",
                    "to navigate corners and turns."
                },
                OnboardStep.SpotlightRegion.NONE,
                OnboardStep.CardSide.CENTER
            )
        ));
    }

    // Level 3 — unlocks moveLeft / moveUp, completing the set.
    // Source: story/between-levels/level-3.md
    private static OnboardFlow level3Flow() {
        return new OnboardFlow(Arrays.asList(
            step(
                "New Commands Unlocked!",
                new String[]{
                    "You unlocked new commands!",
                    "moveLeft  — moves RIVETS one tile to the left",
                    "moveUp    — moves RIVETS one tile upward"
                },
                OnboardStep.SpotlightRegion.NONE,
                OnboardStep.CardSide.CENTER
            ),
            step(
                "Tutorial Completed",
                new String[]{
                    "You have completed the basic tutorial.",
                    "All four directions are now available:",
                    "moveRight, moveDown, moveLeft, moveUp"
                },
                OnboardStep.SpotlightRegion.NONE,
                OnboardStep.CardSide.CENTER
            )
        ));
    }

    // Level 8 — major concept: the repeat command.
    // Source: story/between-levels/level-8.md
    private static OnboardFlow level8Flow() {
        return new OnboardFlow(Arrays.asList(
            step(
                "New Command Unlocked!",
                new String[]{
                    "repeat(N) { command }",
                    "Runs the command inside N times in a row."
                },
                OnboardStep.SpotlightRegion.NONE,
                OnboardStep.CardSide.CENTER
            ),
            step(
                "Seeing it in action",
                new String[]{
                    "Instead of:",
                    "  move right",
                    "  move right",
                    "  move right  (x8 total)",
                    "You write: repeat(8) { move right }"
                },
                OnboardStep.SpotlightRegion.NONE,
                OnboardStep.CardSide.CENTER
            ),
            step(
                "Cleaner Code, Smarter RIVETS",
                new String[]{
                    "Both versions reach the goal — but repeat is cleaner.",
                    "Fewer lines, clearer intent, less room for mistakes.",
                    "Cleaner code earns more stars and a higher score,",
                    "and the better your code, the smarter RIVETS becomes."
                },
                OnboardStep.SpotlightRegion.NONE,
                OnboardStep.CardSide.CENTER
            ),
            // Placeholder spotlight on the game area until a Documentation button exists.
            step(
                "All Commands Unlocked!",
                new String[]{
                    "You now have every command RIVETS can use.",
                    "Need a refresher? Open the documentation to see every command, examples, and how they work.",
                    "Visit: rivets-programming-game-lang-ref.archontis.gr",
                    "The tools are yours. Now it's time to master them."
                },
                OnboardStep.SpotlightRegion.GAME_AREA,
                OnboardStep.CardSide.RIGHT
            )
        ));
    }

    // Level 11 — nested loops. Source: story/between-levels/level-11.md
    private static OnboardFlow level11Flow() {
        return new OnboardFlow(Arrays.asList(
            step(
                "Nested Loops",
                new String[]{
                    "You can put a repeat inside another repeat.",
                    "The inner loop runs fully each time",
                    "the outer loop runs once.",
                    "This is called a nested loop."
                },
                OnboardStep.SpotlightRegion.NONE,
                OnboardStep.CardSide.CENTER
            ),
            step(
                "What it looks like",
                new String[]{
                    "repeat(3) {",
                    "  repeat(3) { move right }",
                    "  move down",
                    "}",
                    "Move right 3×, then down — 3 times total."
                },
                OnboardStep.SpotlightRegion.NONE,
                OnboardStep.CardSide.CENTER
            ),
            step(
                "How it runs step by step",
                new String[]{
                    "Outer loop tick 1: move right 3×, move down",
                    "Outer loop tick 2: move right 3×, move down",
                    "Outer loop tick 3: move right 3×, move down",
                    "Think of the outer loop as rows,",
                    "and the inner loop as tiles per row."
                },
                OnboardStep.SpotlightRegion.NONE,
                OnboardStep.CardSide.CENTER
            ),
            step(
                "Try it!",
                new String[]{
                    "Count how many rows the path has.",
                    "Count how many tiles wide each row is.",
                    "Outer count = rows. Inner count = tiles per row.",
                    "Then write your nested loop."
                },
                OnboardStep.SpotlightRegion.NONE,
                OnboardStep.CardSide.CENTER
            )
        ));
    }

    private static OnboardStep step(String title, String[] lines,
            OnboardStep.SpotlightRegion spotlight, OnboardStep.CardSide cardSide) {
        return new OnboardStep(title, lines, spotlight, cardSide);
    }
}
