/**
 * Write a description of class RunScriptCommand here.
 * 
 * @author (Archontis E. Kostis) 
 */
public class RunScriptCommand implements Command 
{
    // Run Command does not have a label because it is not a command that can be used on scripts
    // Run is a special command that fires when the user clicks the "run" button to run their script
    @Override
    public void execute(RobotActor robot) {
        robot.executeCommands();
    }
    
    @Override
    public String getLabel() {
        return "label is unsupported on RunScriptCommand";
    }
}
