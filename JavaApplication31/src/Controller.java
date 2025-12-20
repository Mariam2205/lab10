import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class Controller implements Controllable {

  private static final String SAVE_FILE = "sudoku_save.ser";
    private int[][] currentGame;
    private int[][] solution;
    private boolean[][] isOriginal;
    private int faults;
    private Random random = new Random();
    private GameLogger logger = new GameLogger();

    @Override
    public boolean[] getCatalog() {
        // Return an array indicating which difficulty levels are available
        return new boolean[]{true, true, true}; // Easy, Medium, Hard
    }

    @Override
    public int[][] getGame(char level) throws Exception {
        // Create a new game based on difficulty level
        int[][] newGame = new int[9][9];
        int cellsToRemove = 0;
        
        switch(Character.toUpperCase(level)) {
            case 'E': 
                cellsToRemove = 15;  // Easy: remove 15 numbers
                break;
            case 'M': 
                cellsToRemove = 20;  // Medium: remove 20 numbers
                break;
            case 'H': 
                cellsToRemove = 25;  // Hard: remove 25 numbers
                break;
            default: 
                throw new IllegalArgumentException("Invalid difficulty level. Use E, M, or H.");
        }
        
        // Generate a solved puzzle
        generateSolvedPuzzle(newGame, 0, 0);
        
        // Store the solution before removing numbers
        solution = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(newGame[i], 0, solution[i], 0, 9);
        }
        
        // Remove numbers to create the puzzle
        removeNumbers(newGame, cellsToRemove);
        
        currentGame = newGame;
        return newGame;
    }

    @Override
    public int[][] getSolution() {
        return solution;
    }

    @Override
    public void driveGames(String sourcePath) throws Exception {
        // In a real implementation, this would load games from a file
        // For now, we'll just create a new game
        getGame('M'); // Default to medium difficulty
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
    public int[][] solveGame(int[][] game) throws Exception {
        int[][] solution = new int[9][9];
        // Copy the game to solution
        for (int i = 0; i < 9; i++) {
            System.arraycopy(game[i], 0, solution[i], 0, 9);
        }
        
        if (solveSudoku(solution)) {
            return solution;
        } else {
            throw new Exception("No solution exists");
        }
    }

    @Override
    public void logUserAction(UserAction userAction) throws IOException {
        logger.logAction(userAction);
    }

    // Helper methods for Sudoku generation and solving
    private boolean generateSolvedPuzzle(int[][] grid, int row, int col) {
        if (col == 9) {
            col = 0;
            row++;
            if (row == 9) {
                return true;
            }
        }
        
        // Skip filled cells
        if (grid[row][col] != 0) {
            return generateSolvedPuzzle(grid, row, col + 1);
        }
        
        // Try numbers 1-9 in random order
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
        // Check row
        for (int x = 0; x < 9; x++) {
            if (grid[row][x] == num) return false;
        }
        
        // Check column
        for (int x = 0; x < 9; x++) {
            if (grid[x][col] == num) return false;
        }
        
        // Check 3x3 box
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

    private void removeNumbers(int[][] grid, int count) {
        while (count > 0) {
            int row = random.nextInt(9);
            int col = random.nextInt(9);
            
            if (grid[row][col] != 0) {
                grid[row][col] = 0;
                count--;
            }
        }
    }
@Override
    public void saveGame() throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(this);
        }
    }
    @Override
    public void loadGame() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            Controller loaded = (Controller) in.readObject();
            this.currentGame = loaded.currentGame;
            this.solution = loaded.solution;
            this.isOriginal = loaded.isOriginal;
            this.faults = loaded.faults;
        }
    }
    @Override
    public int getRemainingCells() {
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
    // Modify the setCellValue method in your GUI to check for faults
    public boolean setCellValue(int row, int col, int value) {
        if (isOriginal[row][col]) {
            return false;
        }
        
        currentGame[row][col] = value;
        if (value != 0 && value != solution[row][col]) {
            faults++;
            if (faults >= 3) {
                // Game over - 3 faults
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
