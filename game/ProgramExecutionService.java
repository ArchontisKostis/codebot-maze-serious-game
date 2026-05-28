public final class ProgramExecutionService {

    private final RobotActor robot;
    private final CodeEditor editor;
    private final TerminalManager terminalManager;
    private final ProgramCompilationPipeline compilationPipeline;
    private final MyWorldSessionState sessionState;
    private final CheatEngine cheatEngine;
    private final Runnable redrawHudAction;
    private final Runnable onGoalReachedAction;

    public ProgramExecutionService(
        RobotActor robot,
        CodeEditor editor,
        TerminalManager terminalManager,
        ProgramCompilationPipeline compilationPipeline,
        MyWorldSessionState sessionState,
        CheatEngine cheatEngine,
        Runnable redrawHudAction,
        Runnable onGoalReachedAction) {
        this.robot = robot;
        this.editor = editor;
        this.terminalManager = terminalManager;
        this.compilationPipeline = compilationPipeline;
        this.sessionState = sessionState;
        this.cheatEngine = cheatEngine;
        this.redrawHudAction = redrawHudAction;
        this.onGoalReachedAction = onGoalReachedAction;
    }

    public void runScript() {
        if (isInteractionLocked()) return;
        if (robot.isRunning()) return;

        String source = editor.getText();
        terminalManager.clear();
        editor.clearExecutingLine();

        if (cheatEngine.tryDispatch(source)) return;

        Program program = compileProgram(source, "~ # [!] Nothing to run");
        if (program == null) {
            return;
        }

        sessionState.incrementAttempts();
        sessionState.setLastProgram(program);
        redrawHudAction.run();
        robot.run(program);
    }

    public void stepScript() {
        if (isInteractionLocked()) return;

        if (robot.isStepping()) {
            robot.stepOnce();
            return;
        }
        if (robot.isRunning()) return;

        String source = editor.getText();
        terminalManager.clear();
        editor.clearExecutingLine();

        Program program = compileProgram(source, "~ # [!] Nothing to step");
        if (program == null) {
            return;
        }

        sessionState.incrementAttempts();
        sessionState.setLastProgram(program);
        redrawHudAction.run();
        robot.startStepping(program);
        robot.stepOnce();
    }

    public void resetEditor() {
        if (isInteractionLocked()) return;
        editor.clearExecutingLine();
        robot.resetToHome();
    }

    private boolean isInteractionLocked() {
        return sessionState.isIntroOverlayActive() || sessionState.isProgramEndOverlayActive();
    }

    private Program compileProgram(String source, String emptyInputMessage) {
        ProgramCompilationPipeline.Result result = compilationPipeline.compile(source);
        switch (result.status) {
            case SUCCESS:
                return result.program;
            case EMPTY_INPUT:
                terminalManager.log(emptyInputMessage);
                return null;
            case EMPTY_PROGRAM:
                terminalManager.log("~ # [!] Program is empty");
                return null;
            case LEX_ERROR:
                terminalManager.log("~ # [LEX ERROR] " + result.errorMessage);
                return null;
            case PARSE_ERROR:
                terminalManager.log("~ # [PARSE ERROR] " + result.errorMessage);
                return null;
            case RUNTIME_ERROR:
                terminalManager.log("~ # [ERROR] " + result.errorMessage);
                return null;
            default:
                terminalManager.log("~ # [ERROR] Unknown compilation outcome");
                return null;
        }
    }
}