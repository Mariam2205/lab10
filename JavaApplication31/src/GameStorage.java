import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

/**
 * Handles storage and retrieval of Sudoku games with different difficulty levels
 */
public class GameStorage {
    private static final String BASE_DIR = "sudoku_games";
    private static final String EASY_DIR = BASE_DIR + "/easy";
    private static final String MEDIUM_DIR = BASE_DIR + "/medium";
    private static final String HARD_DIR = BASE_DIR + "/hard";
    private static final String CURRENT_DIR = BASE_DIR + "/current";
    
    public GameStorage() {
        createDirectories();
    }
    
    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(EASY_DIR));
            Files.createDirectories(Paths.get(MEDIUM_DIR));
            Files.createDirectories(Paths.get(HARD_DIR));
            Files.createDirectories(Paths.get(CURRENT_DIR));
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
        }
    }
    
    public void saveGame(DifficultyEnum difficulty, int[][] board) throws IOException {
        String dir = getDirectoryForDifficulty(difficulty);
        String filename = dir + "/game_" + System.currentTimeMillis() + ".dat";
        saveBoardToFile(board, filename);
    }
    
    public void saveCurrentGame(int[][] board) throws IOException {
        clearCurrentGame();
        String filename = CURRENT_DIR + "/current_game.dat";
        saveBoardToFile(board, filename);
    }
    
    public int[][] loadGame(DifficultyEnum difficulty) throws IOException {
        String dir = getDirectoryForDifficulty(difficulty);
        File[] games = new File(dir).listFiles((d, name) -> name.endsWith(".dat"));
        if (games == null || games.length == 0) {
            throw new IOException("No games found for difficulty: " + difficulty);
        }
        // Load the first available game
        return loadBoardFromFile(games[0].getPath());
    }
    
    public int[][] loadCurrentGame() throws IOException {
        String filename = CURRENT_DIR + "/current_game.dat";
        File file = new File(filename);
        if (!file.exists()) {
            throw new IOException("No current game found");
        }
        return loadBoardFromFile(filename);
    }
    
    public void deleteCurrentGame() throws IOException {
        clearCurrentGame();
    }
    
    public void deleteGame(DifficultyEnum difficulty, String filename) throws IOException {
        Files.deleteIfExists(Paths.get(filename));
    }
    
    public boolean hasCurrentGame() {
        return Files.exists(Paths.get(CURRENT_DIR + "/current_game.dat"));
    }
    
    public boolean hasGamesForAllDifficulties() {
        return hasGamesInDirectory(EASY_DIR) && 
               hasGamesInDirectory(MEDIUM_DIR) && 
               hasGamesInDirectory(HARD_DIR);
    }
    
    public Catalog getCatalog() {
        return new Catalog(hasCurrentGame(), hasGamesForAllDifficulties());
    }
    
    private String getDirectoryForDifficulty(DifficultyEnum difficulty) {
        switch (difficulty) {
            case EASY: return EASY_DIR;
            case MEDIUM: return MEDIUM_DIR;
            case HARD: return HARD_DIR;
            case INCOMPLETE: return CURRENT_DIR;
            default: throw new IllegalArgumentException("Unknown difficulty: " + difficulty);
        }
    }
    
    private boolean hasGamesInDirectory(String dir) {
        File directory = new File(dir);
        if (!directory.exists()) return false;
        File[] games = directory.listFiles((d, name) -> name.endsWith(".dat"));
        return games != null && games.length > 0;
    }
    
    private void saveBoardToFile(int[][] board, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(board);
        }
    }
    
    private int[][] loadBoardFromFile(String filename) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            try {
                return (int[][]) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("Failed to load game: " + e.getMessage());
            }
        }
    }
    
    private void clearCurrentGame() throws IOException {
        Files.deleteIfExists(Paths.get(CURRENT_DIR + "/current_game.dat"));
        Files.deleteIfExists(Paths.get(CURRENT_DIR + "/game.log"));
    }
}
