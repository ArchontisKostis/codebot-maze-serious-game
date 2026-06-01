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
        new Level7(),
        new Level8(),
        new Level9(),
        new Level10(),
        new Level11(),
        new Level12(),
        new Level13(),
        new Level14(),
        new Level15()
    );

    private static final int[] earnedStars = new int[levelsList.size()];

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

    public static void resetProgress() {
        currentLevel = 0;
        for (int i = 0; i < earnedStars.length; i++) {
            earnedStars[i] = 0;
        }
    }

    public static void recordCurrentLevelStars(int stars) {
        earnedStars[currentLevel] = Math.max(earnedStars[currentLevel], Math.max(0, Math.min(3, stars)));
    }

    /** Overwrite every level's earned stars (used by the {@code end tier=} cheat to force a final tier). */
    public static void setAllLevelStars(int stars) {
        int clamped = Math.max(0, Math.min(3, stars));
        for (int i = 0; i < earnedStars.length; i++) {
            earnedStars[i] = clamped;
        }
    }

    public static int getCurrentLevelStars() {
        return earnedStars[currentLevel];
    }

    public static int getTotalStars() {
        int total = 0;
        for (int stars : earnedStars) {
            total += stars;
        }
        return total;
    }

    public static int getMaxStars() {
        return levelsList.size() * 3;
    }

    public static int getLevelCount() {
        return levelsList.size();
    }

    public static int getCurrentLevelIndex() { return currentLevel; }

    public static void setCurrentLevel(int index) {
        currentLevel = Math.max(0, Math.min(index, levelsList.size() - 1));
    }

    public static int getCurrentLevelNumber() {
        return currentLevel + 1;
    }
}
