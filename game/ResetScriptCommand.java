/**
 * Write a description of class ResetScriptCommand here.
 * 
 * @author (Archontis E. Kostis) 
 */
public class ResetScriptCommand implements Command 
{
    // Reset Command does not have a label because it is not a command that can be used on scripts
    // Reset is a special command that fires when the user clicks the "reset" button to reset (clear)s their script
    @Override
    public void execute(RobotActor robot) {
        robot.resetCommandsList();
    }
    
    @Override
    public String getLabel() {
        return "Clearing Script";
    }
}
