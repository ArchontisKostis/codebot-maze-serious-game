import greenfoot.Color;
import greenfoot.Font;
import java.util.ArrayList;
import java.util.List;

public final class ProgramStopOverlayController {

    private static final int PROGRAM_STOP_CARD_W = GameScreenLayout.scale(420);
    private static final int PROGRAM_STOP_CARD_H = GameScreenLayout.scale(210);
    private static final int PROGRAM_STOP_CARD_X = (GameScreenLayout.WORLD_WIDTH - PROGRAM_STOP_CARD_W) / 2;
    private static final int PROGRAM_STOP_CARD_Y = (GameScreenLayout.WORLD_HEIGHT - PROGRAM_STOP_CARD_H) / 2;
    private static final int PROGRAM_STOP_BUTTON_W = GameScreenLayout.scale(96);
    private static final int PROGRAM_STOP_BUTTON_H = GameScreenLayout.scale(34);
    private static final int PROGRAM_STOP_BUTTON_X = PROGRAM_STOP_CARD_X + (PROGRAM_STOP_CARD_W - PROGRAM_STOP_BUTTON_W) / 2;
    private static final int PROGRAM_STOP_BUTTON_Y = PROGRAM_STOP_CARD_Y + PROGRAM_STOP_CARD_H - GameScreenLayout.scale(58);

    private final SimulationWorld world;
    private final RobotActor robot;
    private final MyWorldSessionState sessionState;

    public ProgramStopOverlayController(SimulationWorld world, RobotActor robot, MyWorldSessionState sessionState) {
        this.world = world;
        this.robot = robot;
        this.sessionState = sessionState;
    }

    public void showProgramEndedWithoutGoal() {
        showProgramStopOverlay("Program ended", "The robot did not reach the goal.");
    }

    public void showProgramBlocked(String reason) {
        showProgramStopOverlay("Robot blocked", reason);
    }

    private void showProgramStopOverlay(String title, String message) {
        if (sessionState.isProgramEndOverlayActive() || sessionState.hasGoalAdvanceCountdown()) return;

        sessionState.setProgramEndOverlayActive(true);
        PopupOverlay.Style style = new PopupOverlay.Style(
            UiTheme.BACKDROP,
            UiTheme.CARD_BG,
            UiTheme.HEADER_BAND,
            UiTheme.BORDER,
            UiTheme.BTN_PRIMARY,
            UiTheme.BTN_TEXT,
            UiTheme.CLOSE_GLYPH,
            GameScreenLayout.scale(62),
            GameScreenLayout.scale(42),
            GameScreenLayout.scale(16),
            GameScreenLayout.scale(28),
            GameScreenLayout.scale(24),
            GameScreenLayout.scale(7),
            GameScreenLayout.scale(19),
            GameScreenLayout.scale(18),
            GameScreenLayout.scale(14),
            GameScreenLayout.scale(18),
            GameScreenLayout.scale(22));

        List<PopupOverlay.Button> buttons = new ArrayList<>();
        buttons.add(new PopupOverlay.Button(
            PROGRAM_STOP_BUTTON_X,
            PROGRAM_STOP_BUTTON_Y,
            PROGRAM_STOP_BUTTON_W,
            PROGRAM_STOP_BUTTON_H,
            overlay -> "OK",
            PopupOverlay::close));

        world.addObject(
            new PopupOverlay(
                PROGRAM_STOP_CARD_W,
                PROGRAM_STOP_CARD_H,
                PROGRAM_STOP_CARD_X,
                PROGRAM_STOP_CARD_Y,
                style,
                (overlay, image) -> {
                    image.setColor(UiTheme.TITLE);
                    image.setFont(new Font("SansSerif", true, false, GameScreenLayout.scale(20)));
                    image.drawString(title, overlay.getCardLeft() + GameScreenLayout.scale(24), overlay.getCardTop() + GameScreenLayout.scale(40));

                    image.setColor(UiTheme.BODY);
                    image.setFont(new Font("SansSerif", false, false, GameScreenLayout.scale(15)));
                    image.drawString(message, overlay.getCardLeft() + GameScreenLayout.scale(24), overlay.getCardTop() + GameScreenLayout.scale(96));
                    image.setColor(UiTheme.BODY_MUTED);
                    image.drawString("Close this message to return to the start.", overlay.getCardLeft() + GameScreenLayout.scale(24), overlay.getCardTop() + GameScreenLayout.scale(120));
                },
                buttons,
                () -> {
                    robot.resetToHome();
                    sessionState.setProgramEndOverlayActive(false);
                }),
            GameScreenLayout.WORLD_WIDTH / 2,
            GameScreenLayout.WORLD_HEIGHT / 2);
    }
}