package sudokuproject;

public class SudokuSolver {

    public boolean solve(int[][] board) { return solveInternal(board); }

    public int countSolutions(int[][] board, int limit) { return countInternal(board, limit, 0); }

    private boolean solveInternal(int[][] board) {
        int[] rc = findEmpty(board);
        if (rc == null) return true;
        int r = rc[0], c = rc[1];
        for (int v=1; v<=9; v++) {
            if (isValid(board, r, c, v)) {
                board[r][c] = v;
                if (solveInternal(board)) return true;
                board[r][c] = 0;
            }
        }
        return false;
    }

    private int countInternal(int[][] board, int limit, int found) {
        if (found >= limit) return found;
        int[] rc = findEmpty(board);
        if (rc == null) return found + 1;
        int r = rc[0], c = rc[1];
        for (int v=1; v<=9; v++) {
            if (isValid(board, r, c, v)) {
                board[r][c] = v;
                found = countInternal(board, limit, found);
                board[r][c] = 0;
                if (found >= limit) return found;
            }
        }
        return found;
    }

    private int[] findEmpty(int[][] board) {
        for (int r=0;r<9;r++) for (int c=0;c<9;c++) if (board[r][c] == 0) return new int[]{r,c};
        return null;
    }

    private boolean isValid(int[][] board, int row, int col, int val) {
        for (int c=0;c<9;c++) if (board[row][c] == val) return false;
        for (int r=0;r<9;r++) if (board[r][col] == val) return false;
        int br = (row/3)*3, bc=(col/3)*3;
        for (int r=br;r<br+3;r++) for (int c=bc;c<bc+3;c++) if (board[r][c]==val) return false;
        return true;
    }
}
