import greenfoot.Color;
import greenfoot.Font;
import greenfoot.GreenfootImage;
import java.util.ArrayList;
import java.util.List;

public final class TerminalManager {

    private static final int MAX_TERMINAL_LINES = 5;
    private static final int FONT_SIZE = GameScreenLayout.scale(11);
    private static final int LINE_HEIGHT = GameScreenLayout.scale(12);
    private static final int PAD_X = GameScreenLayout.scale(14);
    private static final int PAD_Y = GameScreenLayout.scale(33);
    private static final Color TEXT_COLOR = new Color(180, 240, 180);
    private static final Color TEXT_BG = new Color(0, 0, 0, 0);

    private final SimulationWorld world;
    private final List<String> lines = new ArrayList<>();

    public TerminalManager(SimulationWorld world) {
        this.world = world;
    }

    public void clear() {
        lines.clear();
        redraw();
    }

    public void log(String line) {
        lines.add(line);
        trimToVisibleLineCount();
        redraw();
    }

    public void redraw() {
        GreenfootImage bg = world.getBackground();
        world.drawTerminalBackground(bg);
        bg.setFont(new Font("Monospaced", false, false, FONT_SIZE));

        int x = SimulationWorld.TERMINAL_X + PAD_X;
        int y = SimulationWorld.TERMINAL_Y + PAD_Y;
        for (String line : lines) {
            bg.setColor(TEXT_COLOR);
            bg.drawString(line, x, y);
            y += LINE_HEIGHT;
        }
    }

    private void trimToVisibleLineCount() {
        while (lines.size() > MAX_TERMINAL_LINES) {
            lines.remove(0);
        }
    }
}