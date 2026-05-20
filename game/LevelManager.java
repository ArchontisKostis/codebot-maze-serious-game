import java.util.List;

public class LevelManager {
    private static int currentLevel = 0;

    private static final List<Level> levelsList = List.of(
        new Level1(),
        new Level2(),
        new Level3(),
        new Level4(),
        new Level5(),
        new Level6(),
        new Level7()
    );

    public static Level getCurrentLevel() {
        return levelsList.get(currentLevel);
    }

    public static boolean hasNextLevel() {
        return currentLevel + 1 < levelsList.size();
    }

    public static void advanceLevel() {
        if (hasNextLevel()) {
            currentLevel++;
        }
    }

    public static void resetToFirstLevel() {
        currentLevel = 0;
    }

    public static int getCurrentLevelIndex() { return currentLevel; }

    public static int getCurrentLevelNumber() {
        return currentLevel + 1;
    }
}
