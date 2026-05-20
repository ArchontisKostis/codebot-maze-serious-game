/**
 * Global game settings. All fields are static so any world or actor can read them
 * without needing a reference to the current world.
 */
public final class Settings {

    public enum AnimationSpeed { SLOW, NORMAL, FAST }

    private static AnimationSpeed animationSpeed = AnimationSpeed.NORMAL;

    private Settings() {}

    public static AnimationSpeed getAnimationSpeed() { return animationSpeed; }

    public static void setAnimationSpeed(AnimationSpeed s) { animationSpeed = s; }

    /** Greenfoot.delay() ticks consumed between each robot move. */
    public static int getAnimationDelay() {
        switch (animationSpeed) {
            case SLOW:  return 25;
            case FAST:  return 5;
            default:    return 15;
        }
    }
}
