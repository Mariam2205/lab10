/* ---------- File: TestSuite.java ---------- */
package sudokuproject;

import java.util.*;

/**
 * Automated tests that exercise the model and generator/solver.
 * Run this as a plain Java application to perform non-interactive tests.
 */
public class TestSuite {
    public static void main(String[] args) {
        SudokuModel model = new SudokuModel();
        SudokuGenerator gen = new SudokuGenerator();
        SudokuSolver solver = new SudokuSolver();

        System.out.println("Starting tests...");

        // Test 1: Generator produces unique-solution puzzle
        int[][][] ps = gen.generatePuzzle(36);
        int[][] sol = ps[0];
        int[][] puzz = ps[1];
        int[][] copy = new int[9][9]; for (int r=0;r<9;r++) System.arraycopy(puzz[r],0,copy[r],0,9);
        int count = solver.countSolutions(copy, 2);
        assert count == 1 : "Generator failed uniqueness test";
        System.out.println("Generator uniqueness test passed");

        // Test 2: Model load & reveal
        boolean[][] mask = new boolean[9][9];
        for (int r=0;r<9;r++) for (int c=0;c<9;c++) mask[r][c] = (puzz[r][c] != 0);
        model.loadFromSolutionAndMask(sol, mask);
        assert Arrays.deepEquals(model.getSolutionCopy(), sol) : "Solution not loaded correctly";
        System.out.println("Model load test passed");

        // Test 3: Pencil mode toggling
        model.clearCell(0,0);
        model.togglePencil(0,0,3);
        assert model.getPencil(0,0).contains(3) : "Pencil add failed";
        model.togglePencil(0,0,3);
        assert !model.getPencil(0,0).contains(3) : "Pencil remove failed";
        System.out.println("Pencil toggle test passed");

        // Test 4: Wrong placement penalizes score
        model.generateNewPuzzle(36);
        int before = model.getScore();
        // find two cells in same row with different numbers to cause conflict
        int r = -1, c1 = -1, c2 = -1;
        int[][] p = model.getPuzzleCopy();
        for (int i=0;i<9 && r==-1;i++) for (int j=0;j<9;j++) if (p[i][j] !=0) { r = i; break; }
        if (r==-1) { System.out.println("No given found for test - skipping"); }
        else {
            // try to place same value in different column
            int val = p[r][0]==0? p[r][1] : p[r][0];
            int targetCol = (p[r][0]==0)?0:1;
            model.clearCell(r,targetCol);
            boolean ok = model.placeNumber(r,targetCol,val);
            assert !ok : "Wrong placement should be rejected";
            assert model.getScore() == before - 1 : "Score was not penalized";
            System.out.println("Wrong placement penalty test passed");
        }

        // Test 5: Hint reveals a cell and costs points
        model.generateNewPuzzle(36);
        int before2 = model.getScore();
        boolean hintOk = model.revealOneCell();
        assert hintOk : "Hint failed to reveal";
        assert model.getScore() == before2 - 5 : "Hint did not cost 5 points";
        System.out.println("Hint cost test passed");

        // Test 6: Reveal solution makes model solved
        model.generateNewPuzzle(36);
        model.revealSolution();
        assert model.isSolved() : "Reveal solution failed";
        System.out.println("Reveal solution test passed");

        System.out.println("All tests passed!");
    }
}
