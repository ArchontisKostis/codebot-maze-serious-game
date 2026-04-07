import greenfoot.*;

/**
 * CommandButton
 * 
 * Represents a clickable button for a robot command.
 */
public class CommandButton extends Actor
{
    private Command command;       // The command this button represents
    private RobotActor robot;      // Reference to the robot to send commands to
    private String label;          // Button Label
    
    /**
     * Constructor
     */
    public CommandButton(String label, Command command, RobotActor robot) {
        this.command = command;
        this.robot = robot;
        this.label = label;
        
        setImage(new GreenfootImage(label, 20, Color.BLACK, Color.LIGHT_GRAY));
    }
    
    /**
     * Act method - checks for mouse click
     */
    public void act() {
        if (!Greenfoot.mouseClicked(this)) return; // early return
        
        // If the command is RunScriptCommand, execute immediately
        if (command instanceof RunScriptCommand || command instanceof ResetScriptCommand || command instanceof DeleteLastCommand ) {
            command.execute(robot);  // Special Commands trigger execution directly
        } 
        else {
            robot.addCommand(command); // normal commands are queued
        }
    }
}