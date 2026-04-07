import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RobotActor
 * 
 * Represents the programmable robot.
 * The robot stores commands, then executes them in sequence.
 */
public class RobotActor extends Actor
{
    // ========================
    // CONSTANTS
    // ========================
    private static final int STEP_SIZE = 20;
    private static final int EXECUTION_DELAY = 20;

    // ========================
    // STATE
    // ========================
    private List<String> commands = new ArrayList<>();
    private boolean isExecuting = false;

    // ========================
    // MAIN LOOP
    // ========================
    public void act()
    {
        handleExecution();
    }

    // ========================
    // EXECUTION HANDLING
    // ========================
    private void handleExecution()
    {
        if (Greenfoot.isKeyDown("space") && !isExecuting) {
            executeCommands();
        }
    }

    // ========================
    // COMMAND SYSTEM
    // ========================
    public void addCommand(String cmd)
    {
        commands.add(cmd);
        System.out.println("Added: " + cmd);

        // Small delay to prevent spam
        Greenfoot.delay(10);
    }

    /**
     * Executes all stored commands in order
     */
    public void executeCommands()
    {
        isExecuting = true;

        for (String cmd : commands) {
            executeSingleCommand(cmd);
            Greenfoot.delay(EXECUTION_DELAY);
        }

        commands.clear();
        isExecuting = false;
    }

    /**
     * Executes ONE command
     */
    private void executeSingleCommand(String cmd)
    {
        int newX = getX();
        int newY = getY();

        switch (cmd) {
            case "UP":    newY -= STEP_SIZE; break;
            case "DOWN":  newY += STEP_SIZE; break;
            case "LEFT":  newX -= STEP_SIZE; break;
            case "RIGHT": newX += STEP_SIZE; break;
        }

        moveTo(newX, newY);
    }

    // ========================
    // MOVEMENT
    // ========================
    /**
     * Moves the robot safely within the game area.
     * Prevents going outside the defined GAME AREA.
     */
    private void moveTo(int x, int y)
    {
        // In Greenfoot, getX() and getY() return the center of the actor, not the top-left corner. 
        // To keep the entire actor visible within the panel, we need to account for half of its width and height.
        // We need this to offset the edges so the actor doesn't visually overflow when it reaches the ends of the world
        int halfWidth = getImage().getWidth() / 2;
        int halfHeight = getImage().getHeight() / 2;
        
        
        // Clamp X coordinate:
        // - Minimum allowed X = left boundary + halfWidth
        // - Maximum allowed X = right boundary - halfWidth
        // This ensures the actor's left and right edges stay inside the game area
        int clampedX = Math.max(MyWorld.GAME_AREA_MIN_X + halfWidth, Math.min(x, MyWorld.GAME_AREA_MAX_X - halfWidth));
        
        // Clamp Y coordinate:
        // - Minimum allowed Y = top boundary + halfHeight
        // - Maximum allowed Y = bottom boundary - halfHeight
        // This ensures the actor's top and bottom edges stay inside the game area
        int clampedY = Math.max(MyWorld.GAME_AREA_MIN_Y + halfHeight, Math.min(y, MyWorld.GAME_AREA_MAX_Y - halfHeight));
    
        // Move the actor to the clamped position
        setLocation(clampedX, clampedY);
    }
}