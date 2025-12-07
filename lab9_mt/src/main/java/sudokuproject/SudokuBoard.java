package sudokuproject;

public class SudokuBoard {
    private final int[][] board;

    public SudokuBoard(int[][] b) {
        if (b == null || b.length != 9) throw new IllegalArgumentException("Board must be 9x9");
        for (int i = 0; i < 9; i++) if (b[i].length != 9) throw new IllegalArgumentException("Board must be 9x9");
        this.board = new int[9][9];
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) this.board[r][c] = b[r][c];
    }

    public int getCell(int r, int c) { return board[r][c]; }

    public int[] getRow(int r) {
        int[] out = new int[9];
        System.arraycopy(board[r], 0, out, 0, 9);
        return out;
    }

    public int[] getCol(int c) {
        int[] out = new int[9];
        for (int i = 0; i < 9; i++) out[i] = board[i][c];
        return out;
    }

    public int[] getBox(int b) {
        int[] out = new int[9];
        int br = (b / 3) * 3;
        int bc = (b % 3) * 3;
        int idx = 0;
        for (int r = br; r < br + 3; r++) for (int c = bc; c < bc + 3; c++) out[idx++] = board[r][c];
        return out;
    }
}
