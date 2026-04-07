/**
 * Write a description of class DeleteLastCommand here.
 * 
 * @author (Archontis E. Kostis) 
 */
public class DeleteLastCommand implements Command 
{
    @Override
    public void execute(RobotActor robot) {
        robot.deleteLastCommand();
    }

    @Override
    public String getLabel() {
        return "Deleting last command";
    }
}
