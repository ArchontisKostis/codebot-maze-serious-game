/**
 * Write a description of class MoveRightCommand here.
 * 
 * @author (Archontis E. Kostis) 
 */
public class MoveRightCommand implements Command 
{
    private String label = "Robot.moveRight();";   // Label is the reprentation of the command as "code" on the script area
    
    @Override
    public void execute(RobotActor robot) {
        robot.moveBy(20, 0);
    }
    
    @Override
    public String getLabel() {
        return this.label;
    }
}
