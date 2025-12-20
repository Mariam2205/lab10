import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GameController implements Viewable {
    private final Verifier verifier = new Verifier();
    private final SudokuSolver solver = new SudokuSolver(verifier);
    private final String BASE_DIR = "sudoku_games/";

    public GameController() {
        // Requirement 3.3: Create folder hierarchy
        try {
            Files.createDirectories(Paths.get(BASE_DIR + "easy"));
            Files.createDirectories(Paths.get(BASE_DIR + "medium"));
            Files.createDirectories(Paths.get(BASE_DIR + "hard"));
            Files.createDirectories(Paths.get(BASE_DIR + "incomplete"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Catalog getCatalog() {
        Catalog cat = new Catalog();
        // Requirement 4: Check for unfinished game
        cat.current = Files.exists(Paths.get(BASE_DIR + "incomplete/game.txt"));
        // Requirement 4: Check if at least one game exists per difficulty
        cat.allModesExist = checkModes();
        return cat;
    }

    @Override
    public Game getGame(DifficultyEnum level) throws Exception {
        String folder = level == DifficultyEnum.INCOMPLETE ? "incomplete" : level.toString().toLowerCase();
        Path dir = Paths.get(BASE_DIR + folder);
        
        try (var stream = Files.list(dir)) {
            Path file = stream.filter(p -> p.toString().endsWith(".txt")).findAny()
                              .orElseThrow(() -> new Exception("No game file found."));
            
            int[][] board = readBoard(file);
            // If loading a new game, save it to the 'incomplete' folder as the current game
            if (level != DifficultyEnum.INCOMPLETE) {
                saveBoard(Paths.get(BASE_DIR + "incomplete/game.txt"), board);
                Files.deleteIfExists(Paths.get(BASE_DIR + "incomplete/log.txt"));
            }
            return new Game(board);
        }
    }

    @Override
    public void driveGames(Game sourceGame) throws Exception {
        // Requirement 3.1: Verify source solution first
        String status = verifier.verify(sourceGame.getBoard());
        if (!status.equals("valid")) {
            throw new Exception("Source solution is " + status.toUpperCase());
        }

        // Requirement 3.2: Generate levels using RandomPairs
        RandomPairs rp = new RandomPairs();
        generateAndSaveLevel("easy", sourceGame.getBoard(), rp.generateDistinctPairs(10));
        generateAndSaveLevel("medium", sourceGame.getBoard(), rp.generateDistinctPairs(20));
        generateAndSaveLevel("hard", sourceGame.getBoard(), rp.generateDistinctPairs(25));
    }

    private void generateAndSaveLevel(String folder, int[][] source, List<int[]> removals) throws IOException {
        int[][] board = new int[9][9];
        for (int i = 0; i < 9; i++) board[i] = source[i].clone();
        for (int[] p : removals) board[p[0]][p[1]] = 0;

        String filename = "sudoku_" + System.currentTimeMillis() + ".txt";
        saveBoard(Paths.get(BASE_DIR + folder + "/" + filename), board);
    }

    @Override
    public String verifyGame(Game game) {
        String result = verifier.verify(game.getBoard());
        // Requirement 3.3: If solved and valid, remove permanently
        if (result.equals("valid")) {
            try {
                Files.deleteIfExists(Paths.get(BASE_DIR + "incomplete/game.txt"));
                Files.deleteIfExists(Paths.get(BASE_DIR + "incomplete/log.txt"));
            } catch (IOException e) { e.printStackTrace(); }
        }
        return result;
    }

    @Override
    public int[] solveGame(Game game) throws Exception {
        return solver.solve(game.getBoard());
    }

    @Override
    public void logUserAction(String userAction) throws IOException {
        // Requirement 8: Log entries written immediately
        Files.write(Paths.get(BASE_DIR + "incomplete/log.txt"), 
                   (userAction + "\n").getBytes(), 
                   StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    // Helper: Logic to check if all difficulty folders contain files
    private boolean checkModes() {
        return hasFile("easy") && hasFile("medium") && hasFile("hard");
    }

    private boolean hasFile(String folder) {
        try (var s = Files.list(Paths.get(BASE_DIR + folder))) {
            return s.anyMatch(p -> p.toString().endsWith(".txt"));
        } catch (IOException e) { return false; }
    }

    private void saveBoard(Path path, int[][] board) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int val : row) sb.append(val).append(" ");
            sb.append("\n");
        }
        Files.write(path, sb.toString().getBytes());
    }

    private int[][] readBoard(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        int[][] board = new int[9][9];
        for (int i = 0; i < 9; i++) {
            String[] nums = lines.get(i).trim().split("\\s+");
            for (int j = 0; j < 9; j++) board[i][j] = Integer.parseInt(nums[j]);
        }
        return board;
    }
}