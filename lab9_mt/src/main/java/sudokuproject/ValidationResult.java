package sudokuproject;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private List<String> rowMessages = new ArrayList<>();
    private List<String> colMessages = new ArrayList<>();
    private List<String> boxMessages = new ArrayList<>();

    public boolean isValid() {
        return rowMessages.isEmpty() && colMessages.isEmpty() && boxMessages.isEmpty();
    }
    public List<String> getRowMessages() { return rowMessages; }
    public List<String> getColMessages() { return colMessages; }
    public List<String> getBoxMessages() { return boxMessages; }
    public void setRowMessages(List<String> r) { this.rowMessages = r; }
    public void setColMessages(List<String> c) { this.colMessages = c; }
    public void setBoxMessages(List<String> b) { this.boxMessages = b; }
}
