package sudokuproject;

import java.util.*;

public class SudokuGenerator {
    private final SudokuSolver solver = new SudokuSolver();
    private final Random rnd = new Random();

    public int[][][] generatePuzzle(int clues) {
        int[][] full = generateFullSolution();
        int[][] puzzle = new int[9][9];
        for (int r=0;r<9;r++) System.arraycopy(full[r],0,puzzle[r],0,9);

        List<int[]> cells = new ArrayList<>();
        for (int r=0;r<9;r++) for (int c=0;c<9;c++) cells.add(new int[]{r,c});
        Collections.shuffle(cells, rnd);

        int toRemove = 81 - clues;
        for (int[] rc : cells) {
            if (toRemove <= 0) break;
            int r = rc[0], c = rc[1];
            int backup = puzzle[r][c];
            puzzle[r][c] = 0;
            int[][] copy = new int[9][9]; for (int i=0;i<9;i++) System.arraycopy(puzzle[i],0,copy[i],0,9);
            int count = solver.countSolutions(copy, 2);
            if (count != 1) puzzle[r][c] = backup; else toRemove--;
        }
        return new int[][][]{full, puzzle};
    }

    private int[][] generateFullSolution() {
        int[][] board = new int[9][9];
        fillBoard(board, 0, 0);
        return board;
    }

    private boolean fillBoard(int[][] board, int r, int c) {
        if (r==9) return true;
        int nr = (c==8)? r+1: r;
        int nc = (c==8)? 0: c+1;
        List<Integer> nums = new ArrayList<>(); for (int i=1;i<=9;i++) nums.add(i);
        Collections.shuffle(nums, rnd);
        for (int v : nums) {
            if (isValid(board, r, c, v)) {
                board[r][c] = v;
                if (fillBoard(board, nr, nc)) return true;
                board[r][c] = 0;
            }
        }
        return false;
    }

    private boolean isValid(int[][] board, int row, int col, int val) {
        for (int c=0;c<9;c++) if (board[row][c] == val) return false;
        for (int r=0;r<9;r++) if (board[r][col] == val) return false;
        int br = (row/3)*3, bc = (col/3)*3;
        for (int r=br;r<br+3;r++) for (int c=bc;c<bc+3;c++) if (board[r][c]==val) return false;
        return true;
    }
}
