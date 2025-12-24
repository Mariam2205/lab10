import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GameController implements Viewable {
    private final Verifier verifier = new Verifier();
    private final SudokuSolver solver = new SudokuSolver(verifier);
    private final String BASE_DIR = "sudoku_games/";

    public GameController() {
        
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
        boolean current = Files.exists(Paths.get(BASE_DIR + "incomplete/game.txt"));
        boolean allModesExist = checkModes();
        Catalog cat = new Catalog(current, allModesExist);
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
            
            if (level != DifficultyEnum.INCOMPLETE) {
                saveBoard(Paths.get(BASE_DIR + "incomplete/game.txt"), board);
                Files.deleteIfExists(Paths.get(BASE_DIR + "incomplete/log.txt"));
            }
            return new Game(board);
        }
    }

    @Override
    public void driveGames(Game sourceGame) throws Exception {
        
        String status = verifier.verify(sourceGame.getBoard());
        if (!status.equals("valid")) {
            throw new Exception("Source solution is " + status.toUpperCase());
        }

        
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
        
        Files.write(Paths.get(BASE_DIR + "incomplete/log.txt"), 
                   (userAction + "\n").getBytes(), 
                   StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    
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