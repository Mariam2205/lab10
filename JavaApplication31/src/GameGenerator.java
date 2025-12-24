import java.util.List;

/**
 * Generates Sudoku games of different difficulty levels from a solved board
 */
public class GameGenerator {
    private final Verifier verifier;
    private final RandomPairs randomPairs;
    private final GameStorage storage;
    
    public GameGenerator() {
        this.verifier = new Verifier();
        this.randomPairs = new RandomPairs();
        this.storage = new GameStorage();
    }
    
    /**
     * Generate games of all difficulty levels from a solved board
     */
    public void generateFromSolved(int[][] solvedBoard) throws Exception {
        // Verify the source solution first
        GameState state = verifier.verifyState(solvedBoard);
        if (state != GameState.VALID) {
            throw new Exception("Source solution is not valid: " + state);
        }
        
        // Generate Easy level (remove 10 cells)
        int[][] easyBoard = createDifficultyBoard(solvedBoard, 10);
        storage.saveGame(DifficultyEnum.EASY, easyBoard);
        
        // Generate Medium level (remove 20 cells)
        int[][] mediumBoard = createDifficultyBoard(solvedBoard, 20);
        storage.saveGame(DifficultyEnum.MEDIUM, mediumBoard);
        
        // Generate Hard level (remove 25 cells)
        int[][] hardBoard = createDifficultyBoard(solvedBoard, 25);
        storage.saveGame(DifficultyEnum.HARD, hardBoard);
    }
    
    /**
     * Create a board with specified number of cells removed
     */
    private int[][] createDifficultyBoard(int[][] solvedBoard, int cellsToRemove) {
        int[][] board = deepCopyBoard(solvedBoard);
        List<int[]> positionsToRemove = randomPairs.generateDistinctPairs(cellsToRemove);
        
        for (int[] pos : positionsToRemove) {
            int row = pos[0];
            int col = pos[1];
            board[row][col] = 0; // Set to empty
        }
        
        return board;
    }
    
    /**
     * Create a deep copy of the board
     */
    private int[][] deepCopyBoard(int[][] original) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, 9);
        }
        return copy;
    }
    
    public GameStorage getStorage() {
        return storage;
    }
}
