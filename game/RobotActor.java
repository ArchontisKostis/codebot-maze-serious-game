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
    private static final int NO_COMMAND_RUNNING = -1;

    // ========================
    // STATE
    // ========================
    private long executionStartTime;    
    private List<Command> commands = new ArrayList<>();
    private boolean isExecuting = false;
    private int currentCommandIndex = 0;
    
    
    // ========================
    // MAIN LOOP
    // ========================
    public void act() {
        if (!isExecuting) return;
    
        if (currentCommandIndex < commands.size()) {
    
            // Update UI BEFORE executing
            MyWorld world = (MyWorld)getWorld();
            world.updateScriptDisplay(getScriptText(), currentCommandIndex);
            
            // Log current command on Terminal Area
            String label = commands.get(currentCommandIndex).getLabel();
            world.logToTerminal("~ # [INFO] Running Command: " + label);
    
            commands.get(currentCommandIndex).execute(this);
            currentCommandIndex++;
    
            Greenfoot.delay(15);
    
        } else {
            long executionTime = System.currentTimeMillis() - executionStartTime;

            MyWorld world = (MyWorld)getWorld();
            world.logToTerminal("~ # Execution finished in " + executionTime + " ms");
        
            commands.clear();
            isExecuting = false;
            currentCommandIndex = 0;
        }
    }
    
    // ========================
    // COMMAND SYSTEM
    // ========================
    public void addCommand(Command cmd)
    {
        commands.add(cmd);
        
        String cmdName = cmd.getClass().getSimpleName();
        System.out.println("Added: " + cmdName);
            
        // 🔥 Update script display
        MyWorld world = (MyWorld)getWorld();
        world.updateScriptDisplay(getScriptText(), NO_COMMAND_RUNNING);
    }

    /**
     * Executes all stored commands in order
     */
    public void executeCommands() {
        if (commands.isEmpty() || isExecuting) return;
    
        isExecuting = true;
        currentCommandIndex = 0;
        
        executionStartTime = System.currentTimeMillis();

        MyWorld world = (MyWorld)getWorld();
        world.logToTerminal("~ # Running Script....");
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
    
    public void moveBy(int dx, int dy) {
        moveTo(getX() + dx, getY() + dy);
    }
    
    public String getScriptText() {
        StringBuilder sb = new StringBuilder();
        
        int lineIndex = 1;  // Line count starts from 1
        for (Command command : commands) {
            String name = command.getLabel();
    
            sb.append(lineIndex++).append(". ").append(name).append("\n");
        }
    
        return sb.toString();
    }
    
    public List<Command> getCommandsList() {
        return this.commands;
    }
    
    public void resetCommandsList() {
        this.commands.clear();
        
        // Force UI refresh
        MyWorld world = (MyWorld)getWorld();
        world.updateScriptDisplay("", -1);
        
        // Clear terminal logs
        world.clearTerminal();
    }
    
    public void deleteLastCommand() {
        if (!commands.isEmpty()) {
            commands.remove(commands.size() - 1);
        }
    
        // Refresh UI
        MyWorld world = (MyWorld)getWorld();
        world.updateScriptDisplay(getScriptText(), -1);
    }
}