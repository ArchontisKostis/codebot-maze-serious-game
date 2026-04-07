/**
 * Write a description of class MoveUpCommand here.
 * 
 * @author (Archontis E. Kostis) 
 */
public class MoveUpCommand implements Command 
{
    private String label = "Robot.moveUp();";   // Label is the reprentation of the command as "code" on the script area
    
    @Override
    public void execute(RobotActor robot) {
        robot.moveBy(0, -20);
    }
    
    @Override
    public String getLabel() {
        return this.label;
    }
}
