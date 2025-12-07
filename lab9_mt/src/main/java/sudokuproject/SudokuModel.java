package sudokuproject;

import java.util.*;

public class SudokuModel {
    private int[][] solution = new int[9][9];
    private int[][] puzzle = new int[9][9];
    private boolean[][] isGiven = new boolean[9][9];
    private Map<String, Set<Integer>> pencil = new HashMap<>();
    private int score = 100;

    private final SudokuGenerator generator = new SudokuGenerator();
    private final SudokuSolver solver = new SudokuSolver();

    public SudokuModel() {}

    public void generateNewPuzzle(int clues) {
        int[][][] ps = generator.generatePuzzle(clues);
        this.solution = ps[0];
        this.puzzle = ps[1];
        pencil.clear();
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) isGiven[r][c] = puzzle[r][c] != 0;
        score = 100;
    }

    public int getScore() { return score; }
    public int getGiven(int r, int c) { return puzzle[r][c]; }
    public boolean isGiven(int r,int c) { return isGiven[r][c]; }
    public void setGivenCell(int r,int c,int v) { puzzle[r][c] = v; isGiven[r][c] = v != 0; }
    public int getValue(int r,int c) { return puzzle[r][c]; }
    public void clearCell(int r,int c) { if (isGiven[r][c]) return; puzzle[r][c] = 0; pencil.remove(key(r,c)); }

    public void revealSolution() { for (int r=0;r<9;r++) for (int c=0;c<9;c++) puzzle[r][c] = solution[r][c]; }
    public boolean placeNumber(int r,int c,int v) {
        if (isGiven[r][c]) return false;
        if (!isValidPlacement(puzzle, r, c, v)) { score -= 1; if (score < 0) score = 0; return false; }
        puzzle[r][c] = v; pencil.remove(key(r,c)); return true;
    }
    public void togglePencil(int r,int c,int v) {
        if (isGiven[r][c]) return; String k = key(r,c);
        Set<Integer> s = pencil.getOrDefault(k, new TreeSet<>()); if (s.contains(v)) s.remove(v); else s.add(v);
        if (s.isEmpty()) pencil.remove(k); else pencil.put(k,s);
    }
    public Set<Integer> getPencil(int r,int c) { return pencil.getOrDefault(key(r,c), Collections.emptySet()); }
    public boolean revealOneCell() {
        List<int[]> empties = new ArrayList<>();
        for (int r=0;r<9;r++) for (int c=0;c<9;c++) if (puzzle[r][c]==0) empties.add(new int[]{r,c});
        if (empties.isEmpty()) return false;
        Collections.shuffle(empties);
        int[] rc = empties.get(0); puzzle[rc[0]][rc[1]] = solution[rc[0]][rc[1]]; pencil.remove(key(rc[0],rc[1])); score -= 5; if (score<0) score=0; return true;
    }
    public boolean isSolved() { for (int r=0;r<9;r++) for (int c=0;c<9;c++) if (puzzle[r][c]==0 || puzzle[r][c]!=solution[r][c]) return false; return true; }

    private static String key(int r,int c){return r+","+c;}
    private boolean isValidPlacement(int[][] board,int row,int col,int val) {
        for (int c=0;c<9;c++) if (board[row][c] == val) return false;
        for (int r=0;r<9;r++) if (board[r][col] == val) return false;
        int br=(row/3)*3, bc=(col/3)*3;
        for (int r=br;r<br+3;r++) for (int c=bc;c<bc+3;c++) if (board[r][c]==val) return false;
        return true;
    }

    // test helpers
    public int[][] getSolutionCopy() { int[][] s=new int[9][9]; for (int r=0;r<9;r++) System.arraycopy(solution[r],0,s[r],0,9); return s; }
    public int[][] getPuzzleCopy() { int[][] p=new int[9][9]; for (int r=0;r<9;r++) System.arraycopy(puzzle[r],0,p[r],0,9); return p; }
    public void loadFromSolutionAndMask(int[][] sol, boolean[][] mask) {
        for (int r=0;r<9;r++) for (int c=0;c<9;c++){ solution[r][c]=sol[r][c]; if (mask[r][c]){puzzle[r][c]=sol[r][c]; isGiven[r][c]=true;} else {puzzle[r][c]=0; isGiven[r][c]=false;} }
        pencil.clear(); score=100;
    }
}
