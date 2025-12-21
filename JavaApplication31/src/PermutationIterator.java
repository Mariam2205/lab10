import java.util.Iterator;

public class PermutationIterator implements Iterator<int[]> {
    private final int[] current = {1, 1, 1, 1, 1};
    private boolean hasNext = true;

    @Override
    public boolean hasNext() { return hasNext; }

    @Override
    public int[] next() {
        int[] result = current.clone();
        int i = 4;
        while (i >= 0) {
            if (current[i] < 9) {
                current[i]++;
                break;
            }
            current[i] = 1;
            i--;
            if (i < 0) hasNext = false;
        }
        return result;
    }
}