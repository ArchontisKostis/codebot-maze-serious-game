/**
 * Write a description of class Command here.
 * 
 * @author (Archontis E. Kostis) 
 */
public interface Command  
{
    void execute(RobotActor robot);
    
    String getLabel();
}
