import greenfoot.*;

public class MyWorld extends World
{
    private RobotActor robot;
    
    public static final int GAME_AREA_MIN_X = 0;
    public static final int GAME_AREA_MAX_X = 600;
    public static final int GAME_AREA_MIN_Y = 0;
    public static final int GAME_AREA_MAX_Y = 450;
    
    public static final int SCRIPT_AREA_MIN_X = 600;
    public static final int SCRIPT_AREA_MAX_X = 200;
    public static final int SCRIPT_AREA_MIN_Y = 0;
    public static final int SCRIPT_AREA_MAX_Y = 450;

    public static final int TERMINAL_AREA_MIN_X = 0;
    public static final int TERMINAL_AREA_MAX_X = 600;
    public static final int TERMINAL_AREA_MIN_Y = 450;
    public static final int TERMINAL_AREA_MAX_Y = 150;
    
    public static final int COMMANDS_AREA_MIN_X = 600;
    public static final int COMMANDS_AREA_MAX_X = 200;
    public static final int COMMANDS_AREA_MIN_Y = 450;
    public static final int COMMANDS_AREA_MAX_Y = 150;
    
    public MyWorld()
    {    
        super(800, 600, 1);

        drawUI();   // Draw layout FIRST
        createGame(); // Then add objects
    }

    /**
     * Draws the UI layout (panels)
     */
    private void drawUI()
    {
        GreenfootImage bg = getBackground();

        drawGameArea();      // LEFT: Game area
        drawScriptArea();    // RIGHT: Script area
        drawTerminalArea();  // BOTTOM LEFT: Output / terminal
        drawCommandsArea();  // BOTTOM RIGHT: Buttons area
    }
    
    /**
     * Draws game area (the area where the robot is and moves)
     */
    private void drawGameArea()
    {
        GreenfootImage bg = getBackground();
        
        // Background
        bg.setColor(Color.LIGHT_GRAY);
        bg.fillRect(
            GAME_AREA_MIN_X, 
            GAME_AREA_MIN_Y, 
            GAME_AREA_MAX_X, 
            GAME_AREA_MAX_Y
        );
        
        // Label (must be bellow the above code so it sits on top of bg color)
        bg.setColor(Color.BLACK);
        bg.drawString("GAME AREA", 250, 20);
    }
    
    /**
     * Draws script area (the area where the user "sees" the code output)
     */
    private void drawScriptArea()
    {
        GreenfootImage bg = getBackground();
        
        // Background
        bg.setColor(Color.WHITE);        
        bg.fillRect(
            SCRIPT_AREA_MIN_X, 
            SCRIPT_AREA_MIN_Y, 
            SCRIPT_AREA_MAX_X, 
            SCRIPT_AREA_MAX_Y
        );
        
        // Label (must be bellow the above code so it sits on top of bg color)
        bg.setColor(Color.BLACK);
        bg.drawString("SCRIPT", 650, 20);
    }
    
    /**
     * Draws script area (the area where the user "sees" the code output)
     */
    private void drawTerminalArea()
    {
        GreenfootImage bg = getBackground();
        
        // Background
        bg.setColor(Color.DARK_GRAY);        
        bg.fillRect(
            TERMINAL_AREA_MIN_X, 
            TERMINAL_AREA_MIN_Y, 
            TERMINAL_AREA_MAX_X, 
            TERMINAL_AREA_MAX_Y
        );
        
        // Label (must be bellow the above code so it sits on top of bg color)
        bg.setColor(Color.BLACK);
        bg.drawString("OUTPUT", 250, 470);
    }

    /**
     * Draws buttons area (the area where the user "selects" the commands)
     */
    private void drawCommandsArea()
    {
        GreenfootImage bg = getBackground();
        
        // Background
        bg.setColor(Color.GRAY);        
        bg.fillRect(
            COMMANDS_AREA_MIN_X, 
            COMMANDS_AREA_MIN_Y, 
            COMMANDS_AREA_MAX_X, 
            COMMANDS_AREA_MAX_Y
        );       
        
        // Label (must be bellow the above code so it sits on top of bg color)
        bg.setColor(Color.BLACK);
        bg.drawString("COMMANDS", 640, 470);
    }
    
    /**
     * Adds game objects
     */
    private void createGame()
    {
        robot = new RobotActor();
        addObject(robot, 100, 100);
        
        createButtons();
    }
    
    private void createButtons() {
        // -----------------------------
        // RUN button (top-right in COMMANDS area)
        // -----------------------------
        CommandButton runBtn = new CommandButton("RUN", robot);
        addObject(runBtn, 720, 465); // centered at top of commands panel
    
        // -----------------------------
        // Direction buttons (2x2 grid below RUN)
        // Layout:
        //  [UP]   [DOWN]
        //  [LEFT] [RIGHT]
        // -----------------------------
        String[][] grid = {
            {"UP", "DOWN"},
            {"LEFT", "RIGHT"}
        };
    
        int startX = 665;  // leftmost X of first column
        int startY = 505;  // top Y of first row
        int gapX = 60;     // horizontal spacing between buttons
        int gapY = 40;     // vertical spacing between buttons
    
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                CommandButton btn = new CommandButton(grid[row][col], robot);
                int x = startX + col * gapX;
                int y = startY + row * gapY;
                addObject(btn, x, y);
            }
        }
    }
    
    /**
     * Displays current script on screen
     */
    public void updateScriptDisplay(String text)
    {
        showText(text, 700, 200);
    }
}