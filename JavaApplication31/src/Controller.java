import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class Controller implements Controllable, Serializable {

  private static final String BASE_DIR = "sudoku_games";
    private static final String INCOMPLETE_DIR = BASE_DIR + File.separator + "incomplete";
    private static final String INCOMPLETE_SAVE_FILE = INCOMPLETE_DIR + File.separator + "game.txt";
    private static final String INCOMPLETE_LOG_FILE = INCOMPLETE_DIR + File.separator + "log.txt";
    private static final long serialVersionUID = 1L;
    private int[][] currentGame;
    private int[][] solution;
    private boolean[][] isOriginal;
    private int faults;
    private transient Random random = new Random();
    private transient GameLogger logger = new GameLogger();
    private String currentGameSourcePath;

    private transient SolveStrategy solveStrategy = new PermutationSolveStrategy();

    private static class SaveState implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int[][] currentGame;
        private final int[][] solution;
        private final boolean[][] isOriginal;
        private final int faults;
        private final String currentGameSourcePath;

        private SaveState(int[][] currentGame, int[][] solution, boolean[][] isOriginal, int faults, String currentGameSourcePath) {
            this.currentGame = currentGame;
            this.solution = solution;
            this.isOriginal = isOriginal;
            this.faults = faults;
            this.currentGameSourcePath = currentGameSourcePath;
        }
    }

    private void ensureGameDirs() throws IOException {
        Files.createDirectories(Paths.get(BASE_DIR, "easy"));
        Files.createDirectories(Paths.get(BASE_DIR, "medium"));
        Files.createDirectories(Paths.get(BASE_DIR, "hard"));
        Files.createDirectories(Paths.get(INCOMPLETE_DIR));
    }

    private void enforceIncompleteFolderState() throws IOException {
        ensureGameDirs();
        Path dir = Paths.get(INCOMPLETE_DIR);

        try (java.nio.file.DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                String name = p.getFileName().toString();
                if (!name.equals("game.txt") && !name.equals("log.txt")) {
                    Files.deleteIfExists(p);
                }
            }
        }

        Path save = Paths.get(INCOMPLETE_SAVE_FILE);
        Path log = Paths.get(INCOMPLETE_LOG_FILE);

        if (Files.exists(save)) {
            if (!Files.exists(log)) {
                Files.writeString(log, "");
            }
        } else {
            Files.deleteIfExists(log);
        }
    }

    private void resetIncompleteLog() throws IOException {
        ensureGameDirs();
        Files.writeString(Paths.get(INCOMPLETE_LOG_FILE), "");
    }

    private static String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (j > 0) sb.append(' ');
                sb.append(board[i][j]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static String originalToString(boolean[][] original) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (j > 0) sb.append(' ');
                sb.append(original[i][j] ? 1 : 0);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static int[][] parseBoard(java.util.List<String> lines, int startIndex) {
        int[][] board = new int[9][9];
        for (int i = 0; i < 9; i++) {
            String[] parts = lines.get(startIndex + i).trim().split("\\s+");
            for (int j = 0; j < 9; j++) {
                board[i][j] = Integer.parseInt(parts[j]);
            }
        }
        return board;
    }

    private static boolean[][] parseOriginal(java.util.List<String> lines, int startIndex) {
        boolean[][] original = new boolean[9][9];
        for (int i = 0; i < 9; i++) {
            String[] parts = lines.get(startIndex + i).trim().split("\\s+");
            for (int j = 0; j < 9; j++) {
                original[i][j] = Integer.parseInt(parts[j]) != 0;
            }
        }
        return original;
    }

    private static String difficultyFolder(char level) {
        switch (Character.toUpperCase(level)) {
            case 'E':
                return "easy";
            case 'M':
                return "medium";
            case 'H':
                return "hard";
            default:
                throw new IllegalArgumentException("Invalid difficulty level. Use E, M, or H.");
        }
    }

    private static void writeBoard(Path file, int[][] board) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (j > 0) sb.append(' ');
                sb.append(board[i][j]);
            }
            sb.append('\n');
        }
        Files.writeString(file, sb.toString());
    }

    private static int[][] readBoard(Path file) throws IOException {
        String content = Files.readString(file);
        String[] lines = content.split("\\R");
        int[][] board = new int[9][9];
        for (int i = 0; i < 9; i++) {
            String[] parts = lines[i].trim().split("\\s+");
            for (int j = 0; j < 9; j++) {
                board[i][j] = Integer.parseInt(parts[j]);
            }
        }
        return board;
    }

    @Override
    public boolean[] getCatalog() {
        
        return new boolean[]{true, true, true}; 
    }

    @Override
    public int[][] getGame(char level) throws Exception {
        char requested = Character.toUpperCase(level);
        if (requested != 'E' && requested != 'M' && requested != 'H') {
            throw new IllegalArgumentException("Invalid difficulty level. Use E, M, or H.");
        }

        ensureGameDirs();

        
        int[][] solved;
        Path sourceFile = Paths.get(BASE_DIR, "source.txt");
        if (Files.exists(sourceFile)) {
            int[][] source = readBoard(sourceFile);
            GameState state = verifyState(source);
            if (state != GameState.VALID) {
                throw new IllegalArgumentException("Source solution in " + sourceFile + " must be VALID. Found: " + state);
            }
            solved = source;
        } else {
            solved = new int[9][9];
            generateSolvedPuzzle(solved, 0, 0);
        }

        long ts = System.currentTimeMillis();
        int[][] easy = copyBoard(solved);
        int[][] medium = copyBoard(solved);
        int[][] hard = copyBoard(solved);

        removeNumbers(easy, 10);
        removeNumbers(medium, 20);
        removeNumbers(hard, 25);

        Path easyFile = Paths.get(BASE_DIR, difficultyFolder('E'), "game_" + ts + "_E.txt");
        Path mediumFile = Paths.get(BASE_DIR, difficultyFolder('M'), "game_" + ts + "_M.txt");
        Path hardFile = Paths.get(BASE_DIR, difficultyFolder('H'), "game_" + ts + "_H.txt");
        writeBoard(easyFile, easy);
        writeBoard(mediumFile, medium);
        writeBoard(hardFile, hard);

        
        this.solution = copyBoard(solved);
        int[][] selected;
        Path selectedFile;
        switch (requested) {
            case 'E':
                selected = easy;
                selectedFile = easyFile;
                break;
            case 'M':
                selected = medium;
                selectedFile = mediumFile;
                break;
            default:
                selected = hard;
                selectedFile = hardFile;
                break;
        }

        this.currentGame = selected;
        this.isOriginal = new boolean[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                this.isOriginal[i][j] = selected[i][j] != 0;
            }
        }

        this.faults = 0;
        this.currentGameSourcePath = selectedFile.toString();
        saveGame();
        resetIncompleteLog();
        return selected;
    }

    private static int[][] copyBoard(int[][] src) {
        int[][] dst = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, 9);
        }
        return dst;
    }

    @Override
    public int[][] getSolution() {
        return solution;
    }

    @Override
    public int[][] getCurrentGame() {
        return currentGame;
    }

    @Override
    public boolean[][] getIsOriginal() {
        return isOriginal;
    }

    @Override
    public void driveGames(String sourcePath) throws Exception {
        
        
        int[][] source = readBoard(Paths.get(sourcePath));
        GameState state = verifyState(source);
        if (state != GameState.VALID) {
            throw new IllegalArgumentException("Source solution must be VALID. Found: " + state);
        }

        
        generateFromSolvedBoard(source, 'E');
        generateFromSolvedBoard(source, 'M');
        generateFromSolvedBoard(source, 'H');
    }

    private void generateFromSolvedBoard(int[][] solvedBoard, char level) throws Exception {
        int[][] newGame = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(solvedBoard[i], 0, newGame[i], 0, 9);
        }

        solution = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(solvedBoard[i], 0, solution[i], 0, 9);
        }

        int cellsToRemove;
        switch (Character.toUpperCase(level)) {
            case 'E':
                cellsToRemove = 10;
                break;
            case 'M':
                cellsToRemove = 20;
                break;
            case 'H':
                cellsToRemove = 25;
                break;
            default:
                throw new IllegalArgumentException("Invalid difficulty level. Use E, M, or H.");
        }

        removeNumbers(newGame, cellsToRemove);

        isOriginal = new boolean[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                isOriginal[i][j] = newGame[i][j] != 0;
            }
        }

        currentGame = newGame;
        ensureGameDirs();
        String folder = difficultyFolder(level);
        Path gameFile = Paths.get(BASE_DIR, folder, "game_" + System.currentTimeMillis() + ".txt");
        writeBoard(gameFile, currentGame);
        currentGameSourcePath = gameFile.toString();
        saveGame();
        resetIncompleteLog();
    }

    @Override
    public boolean[][] verifyGame(int[][] game) {
        boolean[][] isValid = new boolean[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (game[i][j] != 0) {
                    int temp = game[i][j];
                    game[i][j] = 0;
                    isValid[i][j] = isValid(game, i, j, temp);
                    game[i][j] = temp;
                } else {
                    isValid[i][j] = true;
                }
            }
        }
        return isValid;
    }

    @Override
    public GameState verifyState(int[][] game) {
        boolean hasZero = false;

        
        for (int r = 0; r < 9; r++) {
            boolean[] seen = new boolean[10];
            for (int c = 0; c < 9; c++) {
                int v = game[r][c];
                if (v == 0) {
                    hasZero = true;
                    continue;
                }
                if (v < 1 || v > 9) {
                    return GameState.INVALID;
                }
                if (seen[v]) {
                    return GameState.INVALID;
                }
                seen[v] = true;
            }
        }

        
        for (int c = 0; c < 9; c++) {
            boolean[] seen = new boolean[10];
            for (int r = 0; r < 9; r++) {
                int v = game[r][c];
                if (v == 0) {
                    hasZero = true;
                    continue;
                }
                if (v < 1 || v > 9) {
                    return GameState.INVALID;
                }
                if (seen[v]) {
                    return GameState.INVALID;
                }
                seen[v] = true;
            }
        }

        
        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                boolean[] seen = new boolean[10];
                for (int r = br * 3; r < br * 3 + 3; r++) {
                    for (int c = bc * 3; c < bc * 3 + 3; c++) {
                        int v = game[r][c];
                        if (v == 0) {
                            hasZero = true;
                            continue;
                        }
                        if (v < 1 || v > 9) {
                            return GameState.INVALID;
                        }
                        if (seen[v]) {
                            return GameState.INVALID;
                        }
                        seen[v] = true;
                    }
                }
            }
        }

        return hasZero ? GameState.INCOMPLETE : GameState.VALID;
    }

    @Override
    public int[][] solveGame(int[][] game) throws Exception {
        if (solveStrategy == null) {
            solveStrategy = new PermutationSolveStrategy();
        }
        return solveStrategy.solve(game);
    }

    @Override
    public void logUserAction(UserAction userAction) throws IOException {
        logger.logAction(userAction);
    }

    
    private boolean generateSolvedPuzzle(int[][] grid, int row, int col) {
        if (col == 9) {
            col = 0;
            row++;
            if (row == 9) {
                return true;
            }
        }
        
        
        if (grid[row][col] != 0) {
            return generateSolvedPuzzle(grid, row, col + 1);
        }
        
        
        int[] nums = {1,2,3,4,5,6,7,8,9};
        shuffleArray(nums);
        
        for (int num : nums) {
            if (isValid(grid, row, col, num)) {
                grid[row][col] = num;
                if (generateSolvedPuzzle(grid, row, col + 1)) {
                    return true;
                }
                grid[row][col] = 0;
            }
        }
        return false;
    }

    private boolean solveSudoku(int[][] grid) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (grid[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isValid(grid, row, col, num)) {
                            grid[row][col] = num;
                            if (solveSudoku(grid)) {
                                return true;
                            }
                            grid[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValid(int[][] grid, int row, int col, int num) {
        
        for (int x = 0; x < 9; x++) {
            if (grid[row][x] == num) return false;
        }
        
        
        for (int x = 0; x < 9; x++) {
            if (grid[x][col] == num) return false;
        }
        
        
        int boxRowStart = row - row % 3;
        int boxColStart = col - col % 3;
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[boxRowStart + i][boxColStart + j] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private int countSolutions(int[][] grid, int row, int col, int limit) {
        
        while (row < 9 && grid[row][col] != 0) {
            col++;
            if (col == 9) {
                col = 0;
                row++;
            }
        }
        
        
        if (row >= 9) {
            return 1;
        }
        
        int count = 0;
        
        for (int num = 1; num <= 9 && count < limit; num++) {
            if (isValid(grid, row, col, num)) {
                grid[row][col] = num;
                
                
                int nextRow = row;
                int nextCol = col + 1;
                if (nextCol == 9) {
                    nextCol = 0;
                    nextRow++;
                }
                
                count += countSolutions(grid, nextRow, nextCol, limit - count);
                
                
                grid[row][col] = 0;
            }
        }
        
        return count;
    }

    private void removeNumbers(int[][] grid, int count) {
        RandomPairs pairs = new RandomPairs();
        java.util.List<int[]> coords = pairs.generateDistinctPairs(count);

        int removed = 0;
        for (int[] cell : coords) {
            int row = cell[0];
            int col = cell[1];

            if (grid[row][col] == 0) {
                continue;
            }

            int temp = grid[row][col];
            grid[row][col] = 0;

            int[][] testGrid = new int[9][9];
            for (int i = 0; i < 9; i++) {
                System.arraycopy(grid[i], 0, testGrid[i], 0, 9);
            }

            if (countSolutions(testGrid, 0, 0, 2) == 1) {
                removed++;
            } else {
                grid[row][col] = temp;
            }
        }

        
        while (removed < count) {
            java.util.List<int[]> retry = pairs.generateDistinctPairs(count - removed);
            for (int[] cell : retry) {
                if (removed >= count) break;
                int row = cell[0];
                int col = cell[1];
                if (grid[row][col] == 0) continue;

                int temp = grid[row][col];
                grid[row][col] = 0;

                int[][] testGrid = new int[9][9];
                for (int i = 0; i < 9; i++) {
                    System.arraycopy(grid[i], 0, testGrid[i], 0, 9);
                }

                if (countSolutions(testGrid, 0, 0, 2) == 1) {
                    removed++;
                } else {
                    grid[row][col] = temp;
                }
            }
        }
    }
    @Override
    public void saveGame() throws IOException {
        ensureGameDirs();
        
        // Don't save if currentGame is null (game completed and deleted)
        if (currentGame == null) {
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("faults ").append(faults).append('\n');
        sb.append("source ").append(currentGameSourcePath == null ? "" : currentGameSourcePath).append('\n');
        sb.append("current\n").append(boardToString(currentGame));
        sb.append("solution\n").append(boardToString(solution));
        sb.append("original\n").append(originalToString(isOriginal));

        Files.writeString(Paths.get(INCOMPLETE_SAVE_FILE), sb.toString());
        enforceIncompleteFolderState();
    }

    @Override
    public void loadGame() throws IOException, ClassNotFoundException {
        ensureGameDirs();
        enforceIncompleteFolderState();
        java.util.List<String> lines = Files.readAllLines(Paths.get(INCOMPLETE_SAVE_FILE));
        if (lines.size() < 1) {
            throw new IOException("Invalid save file");
        }

        int idx = 0;
        if (!lines.get(idx).startsWith("faults ")) {
            throw new IOException("Invalid save file");
        }
        this.faults = Integer.parseInt(lines.get(idx).substring("faults ".length()).trim());
        idx++;

        if (!lines.get(idx).startsWith("source ")) {
            throw new IOException("Invalid save file");
        }
        String source = lines.get(idx).substring("source ".length());
        this.currentGameSourcePath = source.isBlank() ? null : source;
        idx++;

        if (!lines.get(idx).trim().equals("current")) {
            throw new IOException("Invalid save file");
        }
        idx++;
        this.currentGame = parseBoard(lines, idx);
        idx += 9;

        if (!lines.get(idx).trim().equals("solution")) {
            throw new IOException("Invalid save file");
        }
        idx++;
        this.solution = parseBoard(lines, idx);
        idx += 9;

        if (!lines.get(idx).trim().equals("original")) {
            throw new IOException("Invalid save file");
        }
        idx++;
        this.isOriginal = parseOriginal(lines, idx);

        this.random = new Random();
        this.logger = new GameLogger();
        this.solveStrategy = new PermutationSolveStrategy();
        enforceIncompleteFolderState();
    }

    @Override
    public void deleteCurrentGame() throws IOException {
        ensureGameDirs();

        Files.deleteIfExists(Paths.get(INCOMPLETE_SAVE_FILE));
        Files.deleteIfExists(Paths.get(INCOMPLETE_LOG_FILE));
        if (currentGameSourcePath != null && !currentGameSourcePath.isBlank()) {
            Files.deleteIfExists(Paths.get(currentGameSourcePath));
        }

        enforceIncompleteFolderState();

        currentGame = null;
        solution = null;
        isOriginal = null;
        faults = 0;
        currentGameSourcePath = null;
    }

    @Override
    public int getRemainingCells() {
        if (currentGame == null) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (currentGame[i][j] == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public int getFaults() {
        return faults;
    }

    @Override
    public void setFaults(int faults) {
        this.faults = Math.max(0, faults);
    }

    public boolean setCellValue(int row, int col, int value) {
        if (isOriginal[row][col]) {
            return false;
        }

        currentGame[row][col] = value;
        if (value != 0 && value != solution[row][col]) {
            faults++;
            if (faults >= 3) {

                return false;
            }
        }
        return true;
    }

    private void shuffleArray(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
}
