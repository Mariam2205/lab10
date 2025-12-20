// File: UserAction.java
public class UserAction {
    private String actionType;
    private int row;
    private int col;
    private int value;
    private long timestamp;
    
    public UserAction(String actionType, int row, int col, int value) {
        this.actionType = actionType;
        this.row = row;
        this.col = col;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters
    public String getActionType() { return actionType; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getValue() { return value; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("%s: Cell[%d][%d] = %d", actionType, row, col, value);
    }
}