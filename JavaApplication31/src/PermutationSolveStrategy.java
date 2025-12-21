import java.util.Arrays;

public class PermutationSolveStrategy implements SolveStrategy {

    @Override
    public int[][] solve(int[][] game) throws Exception {
        int[] emptyRows = new int[5];
        int[] emptyCols = new int[5];
        int emptyCount = 0;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (game[r][c] == 0) {
                    if (emptyCount >= 5) {
                        throw new Exception("Solver supports exactly 5 empty cells");
                    }
                    emptyRows[emptyCount] = r;
                    emptyCols[emptyCount] = c;
                    emptyCount++;
                }
            }
        }

        if (emptyCount != 5) {
            throw new Exception("Solver supports exactly 5 empty cells");
        }

        BoardVerifier verifier = new BoardVerifier(game, emptyRows, emptyCols);
        PermutationIterator iterator = new PermutationIterator();

        while (iterator.hasNext()) {
            int[] candidate = iterator.next();
            if (verifier.isValid(candidate)) {
                for (int k = 0; k < 5; k++) {
                    game[emptyRows[k]][emptyCols[k]] = candidate[k];
                }
                return game;
            }
        }

        throw new Exception("No solution exists");
    }

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

    private static final class BoardVerifier {
        private final int[][] board;
        private final int[] posIndex = new int[81];

        private BoardVerifier(int[][] board, int[] emptyRows, int[] emptyCols) {
            this.board = board;
            Arrays.fill(posIndex, -1);
            for (int i = 0; i < 5; i++) {
                posIndex[emptyRows[i] * 9 + emptyCols[i]] = i;
            }
        }

        private int valueAt(int r, int c, int[] candidate) {
            int idx = posIndex[r * 9 + c];
            if (idx >= 0) {
                return candidate[idx];
            }
            return board[r][c];
        }

        private boolean isValid(int[] candidate) {
            int[] seen = new int[10];
            int stamp = 1;

            
            for (int r = 0; r < 9; r++) {
                stamp++;
                for (int c = 0; c < 9; c++) {
                    int v = valueAt(r, c, candidate);
                    if (v == 0) continue;
                    if (seen[v] == stamp) return false;
                    seen[v] = stamp;
                }
            }

            
            for (int c = 0; c < 9; c++) {
                stamp++;
                for (int r = 0; r < 9; r++) {
                    int v = valueAt(r, c, candidate);
                    if (v == 0) continue;
                    if (seen[v] == stamp) return false;
                    seen[v] = stamp;
                }
            }

            
            for (int br = 0; br < 3; br++) {
                for (int bc = 0; bc < 3; bc++) {
                    stamp++;
                    for (int r = br * 3; r < br * 3 + 3; r++) {
                        for (int c = bc * 3; c < bc * 3 + 3; c++) {
                            int v = valueAt(r, c, candidate);
                            if (v == 0) continue;
                            if (seen[v] == stamp) return false;
                            seen[v] = stamp;
                        }
                    }
                }
            }

            return true;
        }
    }
}
