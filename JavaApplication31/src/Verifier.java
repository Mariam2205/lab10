public class Verifier {
    
    
    public String verify(int[][] board) {
        boolean hasZero = false;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int v = board[r][c];
                if (v == 0) {
                    hasZero = true;
                    continue;
                }
                if (v < 1 || v > 9 || !isValidPlacement(board, r, c, v)) {
                    return "invalid " + r + "," + c;
                }
            }
        }
        return hasZero ? "incomplete" : "valid";
    }

    public GameState verifyState(int[][] board) {
        boolean hasZero = false;

        for (int r = 0; r < 9; r++) {
            boolean[] seen = new boolean[10];
            for (int c = 0; c < 9; c++) {
                int v = board[r][c];
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
                int v = board[r][c];
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
                        int v = board[r][c];
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

    
    public boolean isValidWithPermutation(int[][] board, java.util.List<int[]> emptyCells, int[] combo) {
        for (int i = 0; i < emptyCells.size(); i++) {
            int r = emptyCells.get(i)[0];
            int c = emptyCells.get(i)[1];
            int val = combo[i];
            if (!isPossibleVirtual(board, emptyCells, combo, r, c, val)) return false;
        }
        return true;
    }

    private boolean isPossibleVirtual(int[][] board, java.util.List<int[]> cells, int[] combo, int r, int c, int val) {
        for (int i = 0; i < 9; i++) {
            if (i != c && getVirtualVal(board, cells, combo, r, i) == val) return false;
            if (i != r && getVirtualVal(board, cells, combo, i, c) == val) return false;
        }
        int subgridRow = r - r % 3;
        int subgridCol = c - c % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if ((subgridRow + i != r || subgridCol + j != c) && getVirtualVal(board, cells, combo, subgridRow + i, subgridCol + j) == val) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getVirtualVal(int[][] board, java.util.List<int[]> cells, int[] combo, int r, int c) {
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i)[0] == r && cells.get(i)[1] == c) return combo[i];
        }
        return board[r][c];
    }

    private boolean isValidPlacement(int[][] board, int row, int col, int val) {
        for (int i = 0; i < 9; i++) {
            if (i != col && board[row][i] == val) return false;
            if (i != row && board[i][col] == val) return false;
        }
        int subgridRow = row - row % 3;
        int subgridCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if ((subgridRow + i != row || subgridCol + j != col) && board[subgridRow + i][subgridCol + j] == val) {
                    return false;
                }
            }
        }
        return true;
    }
}