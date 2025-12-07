package sudokuproject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultCollector {
    private final List<String> rows = Collections.synchronizedList(new ArrayList<>());
    private final List<String> cols = Collections.synchronizedList(new ArrayList<>());
    private final List<String> boxes = Collections.synchronizedList(new ArrayList<>());

    public void addRow(String s) { rows.add(s); }
    public void addCol(String s) { cols.add(s); }
    public void addBox(String s) { boxes.add(s); }

    public List<String> getRows() { return new ArrayList<>(rows); }
    public List<String> getCols() { return new ArrayList<>(cols); }
    public List<String> getBoxes() { return new ArrayList<>(boxes); }
}
