import greenfoot.Color;

/**
 * UiTheme
 *
 * Single source of truth for the dark "RIVETS terminal" overlay palette shared
 * by every modal overlay and popup card in the game. Centralizing these colors
 * here prevents the palette drift that previously let overlays diverge into a
 * mismatched light "cream" look.
 *
 * <p>All overlays (intro narration panel, onboarding cards, home popups, the
 * in-game program-stop overlay, and the final classification card) reference
 * these constants rather than hardcoding their own values.
 */
public final class UiTheme {

    private UiTheme() {
    }

    /** Dim backdrop drawn behind a modal overlay. */
    public static final Color BACKDROP = new Color(0, 0, 0, 170);

    /** Navy card fill (high alpha so content underneath is fully covered). */
    public static final Color CARD_BG = new Color(18, 22, 30, 245);

    /** Steel band behind a popup title (replaces the legacy light-gray header). */
    public static final Color HEADER_BAND = new Color(28, 36, 48);

    /** Steel border; overlays draw it twice (1px doubled) for a crisp edge. */
    public static final Color BORDER = new Color(65, 100, 150);

    /** Near-white blue used for titles and headings. */
    public static final Color TITLE = new Color(210, 225, 245);

    /** Muted blue-gray used for body text. */
    public static final Color BODY = new Color(175, 195, 218);

    /** Slightly higher-contrast body variant for longer prose blocks. */
    public static final Color BODY_STRONG = new Color(196, 212, 232);

    /** Dimmer body variant for footnotes / secondary captions. */
    public static final Color BODY_MUTED = new Color(135, 152, 172);

    /** Primary (accent) button fill and border. */
    public static final Color BTN_PRIMARY = new Color(52, 90, 128);
    public static final Color BTN_PRIMARY_BORDER = new Color(88, 132, 172);

    /** Secondary ("ghost") button fill and border. */
    public static final Color BTN_GHOST = new Color(38, 48, 58);
    public static final Color BTN_GHOST_BORDER = new Color(65, 82, 98);

    /** Button label color. */
    public static final Color BTN_TEXT = Color.WHITE;

    /** Light close ("X") glyph, legible on the navy card. */
    public static final Color CLOSE_GLYPH = new Color(210, 225, 245);
}
