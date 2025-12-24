import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermutationSolveStrategy implements SolveStrategy {

    @Override
    public int[][] solve(int[][] game) throws Exception {
        List<int[]> emptyCells = new ArrayList<>();
        
        // Find all empty cells
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (game[r][c] == 0) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }

        if (emptyCells.size() != 5) {
            throw new Exception("Solver supports exactly 5 empty cells, found: " + emptyCells.size());
        }

        // Use Flyweight pattern for memory-efficient verification
        FlyweightBoard flyweightBoard = new FlyweightBoard(game, emptyCells);
        PermutationIterator iterator = new PermutationIterator();

        // Single-threaded sequential verification
        while (iterator.hasNext()) {
            int[] candidate = iterator.next();
            if (flyweightBoard.isValid(candidate)) {
                // Apply the solution to the original board
                for (int i = 0; i < emptyCells.size(); i++) {
                    int[] cell = emptyCells.get(i);
                    game[cell[0]][cell[1]] = candidate[i];
                }
                return game;
            }
        }

        throw new Exception("No solution exists");
    }

    /**
     * Iterator pattern implementation for generating permutations
     * Generates all possible combinations of 5 digits (1-9) for the empty cells
     */
    private static final class PermutationIterator implements java.util.Iterator<int[]> {
        private static final int MAX = 9 * 9 * 9 * 9 * 9;
        private final int[] values = new int[5];
        private int counter = 0;

        @Override
        public boolean hasNext() {
            return counter < MAX;
        }

        @Override
        public int[] next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            int x = counter++;
            for (int i = 4; i >= 0; i--) {
                values[i] = (x % 9) + 1;
                x /= 9;
            }
            return values;
        }
    }
}
