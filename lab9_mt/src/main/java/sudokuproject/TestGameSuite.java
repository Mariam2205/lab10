package sudokuproject;

import java.util.*;

public class TestGameSuite {
    public static void main(String[] args) {
        System.out.println("Running Game model tests...");
        SudokuModel model = new SudokuModel();
        SudokuGenerator gen = new SudokuGenerator();
        SudokuSolver solver = new SudokuSolver();

        // generator uniqueness test
        int[][][] ps = gen.generatePuzzle(36);
        int[][] sol = ps[0];
        int[][] puzz = ps[1];
        int[][] copy = new int[9][9]; for (int r=0;r<9;r++) System.arraycopy(puzz[r],0,copy[r],0,9);
        int count = solver.countSolutions(copy, 2);
        assert count == 1 : "Generator uniqueness failed";
        System.out.println("Generator uniqueness passed.");

        // model load/reveal test
        boolean[][] mask = new boolean[9][9];
        for (int r=0;r<9;r++) for (int c=0;c<9;c++) mask[r][c] = puzz[r][c] != 0;
        model.loadFromSolutionAndMask(sol, mask);
        assert Arrays.deepEquals(model.getSolutionCopy(), sol) : "Model load failed";
        System.out.println("Model load passed.");

        // pencil toggle
        model.clearCell(0,0);
        model.togglePencil(0,0,3);
        assert model.getPencil(0,0).contains(3);
        model.togglePencil(0,0,3);
        assert !model.getPencil(0,0).contains(3);
        System.out.println("Pencil toggle passed.");

        // wrong placement penalty
        model.generateNewPuzzle(36);
        int before = model.getScore();
        // find a row with a given then try to place same number in different column
        int r = -1, c = -1;
        int[][] p = model.getPuzzleCopy();
        outer:
        for (int i=0;i<9;i++) for (int j=0;j<9;j++) if (p[i][j] != 0) { r = i; break outer; }
        if (r >= 0) {
            int val = p[r][0] == 0 ? p[r][1] : p[r][0];
            int target = p[r][0] == 0 ? 0 : 1;
            model.clearCell(r, target);
            boolean ok = model.placeNumber(r, target, val);
            assert !ok : "Wrong placement should be rejected";
            assert model.getScore() == before - 1 : "Score not decremented";
            System.out.println("Wrong placement penalty passed.");
        } else {
            System.out.println("Skipping wrong placement penalty test (no given found).");
        }

        // hint cost
        model.generateNewPuzzle(36);
        int before2 = model.getScore();
        boolean hintOk = model.revealOneCell();
        assert hintOk : "Hint failed";
        assert model.getScore() == before2 - 5 : "Hint didn't cost 5";
        System.out.println("Hint cost passed.");

        // reveal solution -> solved
        model.generateNewPuzzle(36);
        model.revealSolution();
        assert model.isSolved();
        System.out.println("Reveal solution passed.");

        System.out.println("All game model tests passed.");
    }
}
