import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;

/**
 * Reads plain text from the OS clipboard for the Load Custom screen's PASTE button.
 *
 * <p>Works in the Greenfoot IDE and standalone JAR builds. In a headless or sandboxed
 * context (e.g. some web exports) the clipboard may be unavailable; any failure is
 * swallowed and {@code null} is returned so the caller can show a friendly message.
 */
public final class ClipboardAccess {

    private ClipboardAccess() {
    }

    /** Returns clipboard text, or {@code null} if empty/unavailable. */
    public static String read() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return null;
            }
            Object data = clipboard.getData(DataFlavor.stringFlavor);
            return data == null ? null : data.toString();
        } catch (Throwable t) {
            // HeadlessException, SecurityException, unsupported flavor, etc.
            return null;
        }
    }
}
