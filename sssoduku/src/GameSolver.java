import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameSolver {
    private final Verifier verifier;

    public GameSolver(Verifier verifier) {
        this.verifier = verifier;
    }

    /**
     * [cite_start]Solves the game using permutations, bounded to exactly 5 empty cells[cite: 224].
     * @return An array of 5 integers representing the solution for the missing cells.
     */
    public int[] solve(Game game) throws InvalidGameException {
        if (game.countEmptyCells() != 5) {
            throw new InvalidGameException("Solver is only supported for exactly 5 empty cells."); 
        }
        
        // 1. Identify empty cells and their coordinates
        List<int[]> emptyCells = new ArrayList<>(5);
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (game.getCellValue(r, c) == 0) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }
        
        // 2. Initialize Iterator [cite: 235]
        PermutationIterator iterator = new PermutationIterator();
        
        // 3. Iterate through all permutations
        int checks = 0;
        while (iterator.hasNext()) {
            int[] permutation = iterator.next();
            checks++;
            
            // 4. Verify the board with the current permutation (Flyweight) [cite: 236, 229]
            // We verify the combination without doing in-place updates on the board to save memory [cite: 250, 251]
            boolean isValid = verifier.isValidWithPermutation(game.getBoard(), emptyCells, permutation);
            
            if (isValid) {
                // Found a solution [cite: 230]
                System.out.println("Solution found after " + checks + " checks.");
                return permutation;
            }
        }
        
        throw new InvalidGameException("No solution found for this puzzle.");
    }
    
    /**
     * Returns the solution in the format required by the Controllable interface:
     * int[][] of {x, y, solution} for each missing cell.
     */
    public int[][] formatSolutionForView(Game game, int[] solution) {
        int[][] result = new int[5][3];
        int idx = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (game.getCellValue(r, c) == 0) {
                    result[idx] = new int[]{r, c, solution[idx]};
                    idx++;
                }
            }
        }
        return result;
    }
}