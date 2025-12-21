
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameLogger {
    private static final String LOG_FILE = "sudoku_game.log";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void logAction(UserAction action) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = dateFormat.format(new Date(action.getTimestamp()));
            String logEntry = String.format("[%s] %s: Cell[%d][%d] = %d%n", 
                timestamp, 
                action.getActionType(), 
                action.getRow(), 
                action.getCol(), 
                action.getValue());
            writer.write(logEntry);
        }
    }
}