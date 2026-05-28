import java.util.LinkedHashMap;
import java.util.Map;

public final class CheatEngine {

    @FunctionalInterface
    interface CheatHandler {
        void execute(String[] args);
    }

    private final TerminalManager terminalManager;
    private final Map<String, CheatHandler> cheats;

    public CheatEngine(TerminalManager terminalManager) {
        this.terminalManager = terminalManager;
        this.cheats = new LinkedHashMap<>();
    }

    public void register(String command, CheatHandler handler) {
        cheats.put(command, handler);
    }

    public boolean tryDispatch(String source) {
        String trimmed = source.trim();
        String[] parts = trimmed.split("\\s+", 2);
        String cmd = parts[0];
        String[] args = (parts.length > 1) ? parts[1].split("\\s+") : new String[0];

        CheatHandler handler = cheats.get(cmd);
        if (handler == null) return false;

        terminalManager.log("~ # [DEV] " + cmd);
        handler.execute(args);
        return true;
    }
}
