import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

public class StorageService {
    private static final Path ROOT_DIR = Paths.get("sudoku_games");
    private static final String CURRENT_FOLDER = "current";
    private static final String GAME_FILE = "game.txt";
    private static final String LOG_FILE = "log.txt";

    public StorageService() throws IOException {
        Files.createDirectories(ROOT_DIR.resolve(CURRENT_FOLDER));
        Files.createDirectories(ROOT_DIR.resolve(DifficultyEnum.EASY.toString().toLowerCase()));
        Files.createDirectories(ROOT_DIR.resolve(DifficultyEnum.MEDIUM.toString().toLowerCase()));
        Files.createDirectories(ROOT_DIR.resolve(DifficultyEnum.HARD.toString().toLowerCase()));
    }
    
    /**
     * [cite_start]Checks storage status for the GUI startup logic [cite: 4]
     */
    public Catalog checkGames() {
        Catalog catalog = new Catalog();
        Path currentPath = ROOT_DIR.resolve(CURRENT_FOLDER).resolve(GAME_FILE);
        // 1. Check for an unfinished game 
        catalog.current = Files.exists(currentPath); 
// 2. Check for at least one game of each difficulty [cite: 78]
        boolean easyExists = hasGame(DifficultyEnum.EASY);
        boolean mediumExists = hasGame(DifficultyEnum.MEDIUM);
        boolean hardExists = hasGame(DifficultyEnum.HARD);
        
        catalog.allModesExist = easyExists && mediumExists && hardExists;
        return catalog;
    }
    
    private boolean hasGame(DifficultyEnum level) {
        try (Stream<Path> files = Files.list(ROOT_DIR.resolve(level.toString().toLowerCase()))) {
            return files.anyMatch(p -> p.toString().endsWith(".txt"));
        } catch (IOException e) {
            return false;
        }
    }

    // --- Game Loading/Saving ---

    public void saveGame(DifficultyEnum level, Game game) throws IOException {
        // Save to the appropriate difficulty folder
        String dirName = level.toString().toLowerCase();
        Path folderPath = ROOT_DIR.resolve(dirName);
        
        // Use a unique name (e.g., timestamp) for the game file in the difficulty folders
        String fileName = System.currentTimeMillis() + "_" + GAME_FILE;
        writeBoardToFile(folderPath.resolve(fileName), game.getBoard());
    }
    
    public void saveCurrentGame(Game game) throws IOException {
        // Save to the 'current' folder 
        Path currentPath = ROOT_DIR.resolve(CURRENT_FOLDER).resolve(GAME_FILE);
        writeBoardToFile(currentPath, game.getBoard());
    }
    
    public Game loadGame(DifficultyEnum level) throws NotFoundException, IOException {
        if (level == DifficultyEnum.INCOMPLETE) {
            return loadCurrentGame();
        }
        
        String dirName = level.toString().toLowerCase();
        Path folderPath = ROOT_DIR.resolve(dirName);
        
        // Load a random game of that difficulty (or the first one found)
        try (Stream<Path> files = Files.list(folderPath)) {
            Path gamePath = files.filter(p -> p.toString().endsWith(".txt")).findAny().orElseThrow(() -> new NotFoundException("No game found for level: " + level));
            int[][] board = readBoardFromFile(gamePath);
            return new Game(board, level);
        }
    }
    
    private Game loadCurrentGame() throws NotFoundException, IOException {
        Path currentPath = ROOT_DIR.resolve(CURRENT_FOLDER).resolve(GAME_FILE);
        if (!Files.exists(currentPath)) {
            throw new NotFoundException("No incomplete game found.");
        }
        int[][] board = readBoardFromFile(currentPath);
        // We set the difficulty to INCOMPLETE for the currently played game
        return new Game(board, DifficultyEnum.INCOMPLETE);
    }
    
    // --- Logging and Undo ---
    // Logs a user action in the 'current' folder [cite: 7, 212]
    public void logUserAction(UserAction action) throws IOException {
        Path logPath = ROOT_DIR.resolve(CURRENT_FOLDER).resolve(LOG_FILE);
        try (FileWriter fw = new FileWriter(logPath.toFile(), true); 
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(action.toString());
        }
    }

    public void undoLastMove(Game currentGame) throws IOException {
        Path logPath = ROOT_DIR.resolve(CURRENT_FOLDER).resolve(LOG_FILE);
        if (!Files.exists(logPath)) return;
        
        List<String> lines = Files.readAllLines(logPath);
        if (lines.isEmpty()) return;
        
        String lastLine = lines.get(lines.size() - 1);
        lines.remove(lines.size() - 1); // Remove the last line [cite: 218]
        
        // Parse the log record: (x, y, val, prev)
        String[] parts = lastLine.replaceAll("[()\\s]", "").split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int prev = Integer.parseInt(parts[3]); // Get the previous value [cite: 215]

        // Apply the inverse change: set the cell back to the 'prev' value [cite: 219]
        currentGame.setCellValue(x, y, prev);

        // Rewrite the log file without the last line
        Files.write(logPath, lines);
        
        // Also update the main game file to reflect the change
        saveCurrentGame(currentGame);
    }
    
    // --- Deletion ---
    
    // Deletes a completely solved and verified game [cite: 72]
    public void deleteGameFromFolder(DifficultyEnum level) throws IOException {
        String dirName = level.toString().toLowerCase();
        Path folderPath = ROOT_DIR.resolve(dirName);
        
        // For simplicity, just delete a random/first game file in that folder.
        // In a real application, you'd delete the specific file that was loaded.
        try (Stream<Path> files = Files.list(folderPath)) {
            files.filter(p -> p.toString().endsWith(".txt")).findFirst().ifPresent(file -> {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    // Handle deletion error
                }
            });
        }
    }
    
    public void deleteCurrentGame() throws IOException {
        // Delete the game and the log file from the 'current' folder [cite: 72]
        Path currentPath = ROOT_DIR.resolve(CURRENT_FOLDER).resolve(GAME_FILE);
        Path logPath = ROOT_DIR.resolve(CURRENT_FOLDER).resolve(LOG_FILE);

        Files.deleteIfExists(currentPath);
        Files.deleteIfExists(logPath);
    }
    
    // --- Private Helper Methods ---

    private void writeBoardToFile(Path path, int[][] board) throws IOException {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(path))) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    out.print(board[i][j]);
                }
                out.println();
            }
        }
    }

    private int[][] readBoardFromFile(Path path) throws IOException {
        int[][] board = new int[9][9];
        List<String> lines = Files.readAllLines(path);
        for (int i = 0; i < 9; i++) {
            String line = lines.get(i).trim();
            for (int j = 0; j < 9; j++) {
                board[i][j] = Character.getNumericValue(line.charAt(j));
            }
        }
        return board;
    }
}