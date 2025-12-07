package sudokuproject;

import java.util.*;

public class ColValidator extends Validator {
    private final int colIndex;

    public ColValidator(SudokuBoard b, ResultCollector coll, int colIndex) {
        super(b, coll); this.colIndex = colIndex;
    }

    @Override
    public void validate() {
        int[] col = board.getCol(colIndex);
        Map<Integer, List<Integer>> pos = new HashMap<>();
        for (int r = 0; r < 9; r++) {
            int v = col[r];
            if (v <= 0) continue;
            pos.computeIfAbsent(v, k -> new ArrayList<>()).add(r + 1);
        }
        for (Map.Entry<Integer, List<Integer>> e : pos.entrySet()) {
            if (e.getValue().size() > 1) {
                String s = String.format("COL %d, #%d, %s", colIndex + 1, e.getKey(), joinNoSpaces(e.getValue()));
                collector.addCol(s);
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
