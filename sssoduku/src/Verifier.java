import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Verifier {
    public enum State { VALID, INVALID, INCOMPLETE } 

    /**
     * Verifies a fully solved board (9x9) for use as a source solution.
     * [cite_start]Throws an exception if not VALID[cite: 21].
     */
    public void verifySourceSolution(int[][] sourceBoard) throws SolutionInvalidException {
        State state = verify(sourceBoard);
        if (state != State.VALID) {
           // Source solution must be VALID, not INVALID or INCOMPLETE [cite: 20, 21]
            throw new SolutionInvalidException("Provided source solution is " + state.toString() + ".");
        }
    }
    
    /**
     * Verifies the current state of a board.
     * [cite_start]@return VALID (solved, correct), INVALID (mistakes), or INCOMPLETE (empty cells) [cite: 8, 10]
     */
    public State verify(int[][] board) {
        boolean isComplete = true;
        
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    isComplete = false; // Zero digited cells mean INCOMPLETE [cite: 9]
                    continue;
                }
                
                // Check if the current number is valid in its current position
                if (!isValidPlacement(board, i, j, board[i][j])) {
                    return State.INVALID;
                }
            }
        }
        
        return isComplete ? State.VALID : State.INCOMPLETE;
    }
    
    // Checks if the value 'num' is valid at board[row][col], *assuming* board[row][col] is 'num'
    private boolean isValidPlacement(int[][] board, int row, int col, int num) {
        // We only check for duplicates, as the board already contains 'num' at (row, col)
        
        // Check row and column for duplicates
        for (int i = 0; i < 9; i++) {
            if (i != col && board[row][i] == num) return false; // Duplicate in row
            if (i != row && board[i][col] == num) return false; // Duplicate in column
        }

        // Check 3x3 box for duplicates
        int startRow = row - row % 3;
        int startCol = col - col % 3;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int currRow = startRow + i;
                int currCol = startCol + j;
                if (currRow != row || currCol != col) {
                    if (board[currRow][currCol] == num) return false; // Duplicate in box
                }
            }
        }

        return true;
    }
    
    /**
     * Finds invalid duplicates for the 'verifyGame' method in the Viewable interface.
     * Returns a string representation of the board state and location of invalid cells.
     */
    public String getVerificationResult(Game game) {
        int[][] board = game.getBoard();
        State state = verify(board);
        if (state == State.VALID) return "VALID";
        if (state == State.INCOMPLETE) return "INCOMPLETE";
        
        // If INVALID, find duplicates (simple implementation: find all cells that are part of a violation)
        StringBuilder sb = new StringBuilder("INVALID. Duplicates found at: ");
        boolean foundOne = false;
        
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int num = board[i][j];
                if (num != 0 && !isValidPlacement(board, i, j, num)) {
                    sb.append(String.format("(%d,%d) ", i, j));
                    foundOne = true;
                }
            }
        }
        return foundOne ? sb.toString().trim() : "INVALID (unknown reason)";
    }
    
    /**
     * Checks if a board is valid with a given permutation applied (Flyweight check).
     */
    public boolean isValidWithPermutation(int[][] originalBoard, List<int[]> emptyCells, int[] permutation) {
        // Create a temporary board (or use a flyweight approach)
        int[][] tempBoard = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(originalBoard[i], 0, tempBoard[i], 0, 9);
        }

        // Apply the permutation
        for (int k = 0; k < 5; k++) {
            int row = emptyCells.get(k)[0];
            int col = emptyCells.get(k)[1];
            tempBoard[row][col] = permutation[k];
        }
        
        // Check if the resulting board is VALID
        return verify(tempBoard) == State.VALID;
    }
}