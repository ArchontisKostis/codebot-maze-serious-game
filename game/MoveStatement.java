/**
 * MoveStatement
 *
 * AST leaf node representing a single robot movement in one direction.
 *
 * Grammar:
 *   move ::= "moveUp" | "moveDown" | "moveLeft" | "moveRight"
 */
public class MoveStatement implements Statement {

    public enum Direction { UP, DOWN, LEFT, RIGHT }

    public final Direction direction;

    public MoveStatement(Direction direction) {
        this.direction = direction;
    }

    /** Canonical source text for this statement (used in terminal logging). */
    public String label() {
        switch (direction) {
            case UP:    return "moveUp";
            case DOWN:  return "moveDown";
            case LEFT:  return "moveLeft";
            case RIGHT: return "moveRight";
            default:    return "move?";
        }
    }
}
