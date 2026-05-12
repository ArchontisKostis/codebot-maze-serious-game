import java.util.List;

/**
 * Write a description of class LevelManager here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class LevelManager {
    private static int currentLevel = 0;
    
    private static List<Level> levelsList = List.of(
        new Level1()
    );

    public static Level getNextLevel() {
        int nextLevelIndex = currentLevel + 1;
        
        return levelsList.get(nextLevelIndex);
    }
    
    public static Level getCurrentLevel() {        
        return levelsList.get(currentLevel);
    }
    
    public static int getCurrentLevelIndex() { return currentLevel; }
    
    public static int getCurrentLevelNumber() { 
        // Array index starts at 0 so for level number we need to do +1
        return currentLevel + 1; 
    }
}
