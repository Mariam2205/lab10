import java.util.ArrayList;
import java.util.List;

public class SudokuSolver {
    private final Verifier verifier;

    public SudokuSolver(Verifier v) { this.verifier = v; }

    public int[] solve(int[][] board) throws Exception {
        List<int[]> emptyCells = new ArrayList<>();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] == 0) emptyCells.add(new int[]{r, c});
            }
        }

        if (emptyCells.size() != 5) {
            throw new Exception("Solver only works with 5 empty cells");
        }

        PermutationIterator iter = new PermutationIterator();
        while (iter.hasNext()) {
            int[] combo = iter.next();
            // Flyweight: Check board validity without modification
            if (verifier.isValidWithPermutation(board, emptyCells, combo)) {
                return combo;
            }
        }
        throw new Exception("No solution found.");
    }
}