package sudokuproject;

public abstract class Validator {
    protected SudokuBoard board;
    protected ResultCollector collector;

    protected Validator(SudokuBoard board, ResultCollector collector) {
        this.board = board;
        this.collector = collector;
    }

    public abstract void validate();
}
