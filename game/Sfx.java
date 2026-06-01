import greenfoot.*;

/**
 * Centralized UI sound effects. Files live in the scenario's {@code sounds/} folder
 * and are referenced by filename, as expected by {@link Greenfoot#playSound(String)}.
 */
public final class Sfx {
    /** Default click sound used by every button unless it opts into a specific one. */
    public static final String BUTTON_CLICK = "generic-btn-click.mp3";
    public static final String RUN_CLICK    = "run-btn-click-sound.mp3";
    public static final String RESET_CLICK  = "reset-code-btn-click-sound.mp3";

    private Sfx() {}

    /** Play a sound by filename, swallowing any error so the UI keeps working if it's missing. */
    public static void play(String soundFile) {
        if (soundFile == null) return;
        try {
            Greenfoot.playSound(soundFile);
        } catch (RuntimeException e) {
            // Sound missing or unplayable — ignore.
        }
    }

    /** Play the generic button-click sound. */
    public static void buttonClick() {
        play(BUTTON_CLICK);
    }
}
