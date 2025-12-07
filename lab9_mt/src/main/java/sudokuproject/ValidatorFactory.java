package sudokuproject;

import java.util.*;

public class ValidatorFactory {

    public List<Validator> createAllValidators(SudokuBoard board, ResultCollector coll) {
        List<Validator> list = new ArrayList<>();
        for (int r = 0; r < 9; r++) list.add(new RowValidator(board, coll, r));
        for (int c = 0; c < 9; c++) list.add(new ColValidator(board, coll, c));
        for (int b = 0; b < 9; b++) list.add(new BoxValidator(board, coll, b));
        return list;
    }

    public Validator createRowValidator(SudokuBoard board, ResultCollector coll, int rowIndex) {
        return new RowValidator(board, coll, rowIndex);
    }

    public Validator createColValidator(SudokuBoard board, ResultCollector coll, int colIndex) {
        return new ColValidator(board, coll, colIndex);
    }

    public Validator createBoxValidator(SudokuBoard board, ResultCollector coll, int boxIndex) {
        return new BoxValidator(board, coll, boxIndex);
    }
}
