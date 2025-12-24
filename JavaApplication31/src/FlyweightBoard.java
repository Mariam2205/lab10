import java.util.List;

/**
 * Flyweight pattern implementation for Sudoku board verification.
 * This class allows verification of board states without creating
 * multiple board copies, saving memory during permutation checking.
 */
public class FlyweightBoard {
    private final int[][] originalBoard;
    private final List<int[]> emptyCells;
    
    public FlyweightBoard(int[][] board, List<int[]> emptyCells) {
        this.originalBoard = board;
        this.emptyCells = emptyCells;
    }
    
    /**
     * Get the value at a specific position, considering the current permutation
     */
    public int getValue(int row, int col, int[] permutation) {
        // Check if this position is one of the empty cells
        for (int i = 0; i < emptyCells.size(); i++) {
            int[] cell = emptyCells.get(i);
            if (cell[0] == row && cell[1] == col) {
                return permutation[i];
            }
        }
        return originalBoard[row][col];
    }
    
    /**
     * Verify if the board with the given permutation is valid
     */
    public boolean isValid(int[] permutation) {
        // Check rows
        for (int r = 0; r < 9; r++) {
            boolean[] seen = new boolean[10];
            for (int c = 0; c < 9; c++) {
                int v = getValue(r, c, permutation);
                if (v == 0) continue;
                if (v < 1 || v > 9) return false;
                if (seen[v]) return false;
                seen[v] = true;
            }
        }
        
        // Check columns
        for (int c = 0; c < 9; c++) {
            boolean[] seen = new boolean[10];
            for (int r = 0; r < 9; r++) {
                int v = getValue(r, c, permutation);
                if (v == 0) continue;
                if (v < 1 || v > 9) return false;
                if (seen[v]) return false;
                seen[v] = true;
            }
        }
        
        // Check 3x3 subgrids
        for (int br = 0; br < 3; br++) {
            for (int bc = 0; bc < 3; bc++) {
                boolean[] seen = new boolean[10];
                for (int r = br * 3; r < br * 3 + 3; r++) {
                    for (int c = bc * 3; c < bc * 3 + 3; c++) {
                        int v = getValue(r, c, permutation);
                        if (v == 0) continue;
                        if (v < 1 || v > 9) return false;
                        if (seen[v]) return false;
                        seen[v] = true;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Get the original board reference (no copy for memory efficiency)
     */
    public int[][] getOriginalBoard() {
        return originalBoard;
    }
    
    /**
     * Get the list of empty cells
     */
    public List<int[]> getEmptyCells() {
        return emptyCells;
    }
}
