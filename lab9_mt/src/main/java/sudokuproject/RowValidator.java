package sudokuproject;

import java.util.*;

public class RowValidator extends Validator {
    private final int rowIndex; // 0..8

    public RowValidator(SudokuBoard b, ResultCollector coll, int rowIndex) {
        super(b, coll); this.rowIndex = rowIndex;
    }

    @Override
    public void validate() {
        int[] row = board.getRow(rowIndex);
        Map<Integer, List<Integer>> pos = new HashMap<>();
        for (int c = 0; c < 9; c++) {
            int v = row[c];
            if (v <= 0) continue; // ignore empty/invalid
            pos.computeIfAbsent(v, k -> new ArrayList<>()).add(c + 1);
        }
        for (Map.Entry<Integer, List<Integer>> e : pos.entrySet()) {
            if (e.getValue().size() > 1) {
                String s = String.format("ROW %d, #%d, %s", rowIndex + 1, e.getKey(), joinNoSpaces(e.getValue()));
                collector.addRow(s);
            }
        }
    }

    private String joinNoSpaces(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}
