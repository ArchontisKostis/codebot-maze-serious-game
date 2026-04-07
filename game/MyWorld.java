import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

public class MyWorld extends World
{
    private RobotActor robot;
    private List<String> terminalLogs = new ArrayList<>();
    
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
        // RUN button executes all stored commandss
        // -----------------------------
        CommandButton runBtn = new CommandButton("RUN", new RunScriptCommand(), robot);
        addObject(runBtn, 720, 465); // centered at top of commands panel
        
        CommandButton resetBtn = new CommandButton("RESET", new ResetScriptCommand(), robot);
        addObject(resetBtn, 770, 465); // move right of RUN
        
        CommandButton deleteLastCmdBtn = new CommandButton("DEL", new DeleteLastCommand(), robot);
        addObject(deleteLastCmdBtn, 780, 430); // move right of RUN
    
        // -----------------------------
        // Direction buttons (2x2 grid below RUN)
        // Layout:
        //  [UP]   [DOWN]
        //  [LEFT] [RIGHT]
        // -----------------------------
        Command[][] commands = {
            { new MoveUpCommand(), new MoveDownCommand() },
            { new MoveLeftCommand(), new MoveRightCommand() }
        };
        
        String[][] labels = {
            { "UP", "DOWN" },
            { "LEFT", "RIGHT" }
        };
    
        int startX = 665, startY = 505, gapX = 60, gapY = 40;

        for (int row = 0; row < commands.length; row++) {
            for (int col = 0; col < commands[row].length; col++) {
                addObject(new CommandButton(labels[row][col], commands[row][col], robot),
                          startX + col * gapX, startY + row * gapY);
            }
        }
    }
    
    /**
     * Updates the terminal
     */
    public void updateTerminalDisplay() {
        GreenfootImage bg = getBackground();
    
        // Clear terminal area
        bg.setColor(Color.DARK_GRAY);
        bg.fillRect(
            TERMINAL_AREA_MIN_X,
            TERMINAL_AREA_MIN_Y,
            TERMINAL_AREA_MAX_X,
            TERMINAL_AREA_MAX_Y
        );
    
        // Title
        bg.setColor(Color.WHITE);
        bg.drawString("OUTPUT", 250, 470);
    
        int startY = 490;
        int lineHeight = 15;
    
        for (int i = 0; i < terminalLogs.size(); i++) {
            bg.drawString(terminalLogs.get(i), 10, startY + i * lineHeight);
        }
    }
    
    /**
     * Displays current script on screen
     */
    public void updateScriptDisplay(String text, int activeLine)
    {
        GreenfootImage bg = getBackground();
    
        // Clear script area
        bg.setColor(Color.WHITE);
        bg.fillRect(
            SCRIPT_AREA_MIN_X, 
            SCRIPT_AREA_MIN_Y, 
            SCRIPT_AREA_MAX_X, 
            SCRIPT_AREA_MAX_Y
        );
    
        // Title
        bg.setColor(Color.BLACK);
        bg.drawString("SCRIPT", 650, 20);
    
        String[] lines = text.split("\n");
    
        int startY = 50;
        int lineHeight = 20;
    
        for (int i = 0; i < lines.length; i++) {
    
            // Highlight current line
            if (i == activeLine) {
                bg.setColor(Color.YELLOW);
                bg.fillRect(600, startY + i * lineHeight - 15, 200, 20);
            }
    
            bg.setColor(Color.BLACK);
            bg.drawString(lines[i], 610, startY + i * lineHeight);
        }
    }
    
    public void logToTerminal(String message) {
        terminalLogs.add(message);
    
        // Optional: limit size (like real terminal)
        if (terminalLogs.size() > 10) {
            terminalLogs.remove(0);
        }
    
        updateTerminalDisplay();
    }
    
    public void clearTerminal() {
        terminalLogs.clear();
        updateTerminalDisplay();
    }
}