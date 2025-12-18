

public class UserAction {
    public final int x, y, value, previousValue;

    public UserAction(int x, int y, int value, int previousValue) {
        this.x = x;
        this.y = y;
        this.value = value;
        this.previousValue = previousValue;
    }
    
    // String representation for logging in the Viewable interface
    @Override
    public String toString() {
        // (x, y, val, prev) format
     return String.format("(%d, %d, %d, %d)", x, y, value, previousValue); 
    }
}
