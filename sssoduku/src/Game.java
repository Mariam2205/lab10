public class Game {
    private final int[][] board; // 9x9 board, 0 means empty/incomplete [cite: 9, 10]
    private final DifficultyEnum difficulty;
    
    // Copy constructor
    public Game(Game other) {
        this.difficulty = other.difficulty;
        this.board = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(other.board[i], 0, this.board[i], 0, 9);
        }
    }

    public Game(int[][] board, DifficultyEnum difficulty) {
        // Assume board is a valid 9x9 array
        this.board = board;
        this.difficulty = difficulty;
    }

    public int getCellValue(int row, int col) {
        return board[row][col];
    }
    
    // Updates a cell value and returns the previous value (used for logging/undo)
    public int setCellValue(int row, int col, int value) {
        int previous = board[row][col];
        board[row][col] = value;
        return previous;
    }

    public int[][] getBoard() {
        return board;
    }

    public DifficultyEnum getDifficulty() {
        return difficulty;
    }

    public boolean isCompleted() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public int countEmptyCells() {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    count++;
                }
            }
        }
        return count;
    }
}