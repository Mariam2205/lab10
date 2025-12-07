package sudokuproject;

import java.util.*;

public class BoxValidator extends Validator {
    private final int boxIndex;

    public BoxValidator(SudokuBoard b, ResultCollector coll, int boxIndex) {
        super(b, coll); this.boxIndex = boxIndex;
    }

    @Override
    public void validate() {
        int[] box = board.getBox(boxIndex);
        Map<Integer, List<Integer>> pos = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            int v = box[i];
            if (v <= 0) continue;
            pos.computeIfAbsent(v, k -> new ArrayList<>()).add(i + 1);
        }
        for (Map.Entry<Integer, List<Integer>> e : pos.entrySet()) {
            if (e.getValue().size() > 1) {
                String s = String.format("BOX %d, #%d, %s", boxIndex + 1, e.getKey(), joinNoSpaces(e.getValue()));
                collector.addBox(s);
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
