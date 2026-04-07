import greenfoot.*;

/**
 * CommandButton
 * 
 * Represents a clickable button for a robot command.
 */
public class CommandButton extends Actor
{
    private String command;       // The command this button represents
    private RobotActor robot;     // Reference to the robot to send commands to
    
    /**
     * Constructor
     */
    public CommandButton(String command, RobotActor robot) {
        this.command = command;
        this.robot = robot;
        setImage(new GreenfootImage(command, 20, Color.BLACK, Color.LIGHT_GRAY));
    }
    
    /**
     * Act method - checks for mouse click
     */
    public void act() {
        if (!Greenfoot.mouseClicked(this)) return; // early return
        
        handleMouseClick();
    }
    
    private void handleMouseClick() {
        if (command.equals("RUN")) {
            robot.executeCommands();  // run all commands
        } else {
            robot.addCommand(command); // add directional commands
        }
    }
}