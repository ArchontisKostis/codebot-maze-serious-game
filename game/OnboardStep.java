public class OnboardStep {

    public enum SpotlightRegion {
        NONE,
        GAME_AREA,
        CODE_EDITOR,
        CONTROLS,
        TERMINAL;

        /** Returns {x, y, w, h} in world pixels, or null for NONE. */
        public int[] toRect() {
            switch (this) {
                case GAME_AREA:
                    return new int[]{
                        0,
                        GameScreenLayout.HUD_STRIP_H,
                        GameScreenLayout.GAME_AREA_WIDTH_PX,
                        GameScreenLayout.GAME_AREA_HEIGHT_PX
                    };
                case CODE_EDITOR:
                    return new int[]{
                        GameScreenLayout.SCRIPT_AREA_X,
                        GameScreenLayout.SCRIPT_AREA_Y,
                        GameScreenLayout.SCRIPT_AREA_W,
                        GameScreenLayout.SCRIPT_AREA_H
                    };
                case CONTROLS:
                    return new int[]{
                        GameScreenLayout.CONTROLS_X,
                        GameScreenLayout.CONTROLS_Y,
                        GameScreenLayout.CONTROLS_W,
                        GameScreenLayout.CONTROLS_H
                    };
                case TERMINAL:
                    return new int[]{
                        GameScreenLayout.TERMINAL_X,
                        GameScreenLayout.TERMINAL_Y,
                        GameScreenLayout.TERMINAL_W,
                        GameScreenLayout.TERMINAL_H
                    };
                default:
                    return null;
            }
        }
    }

    public enum CardSide { CENTER, RIGHT, LEFT }

    public final String title;
    public final String[] lines;
    public final SpotlightRegion spotlight;
    public final CardSide cardSide;

    public OnboardStep(String title, String[] lines, SpotlightRegion spotlight, CardSide cardSide) {
        this.title = title;
        this.lines = lines;
        this.spotlight = spotlight;
        this.cardSide = cardSide;
    }
}
