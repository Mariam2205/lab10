import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * [cite_start]Generates permutations of 5 numbers (1-9) on the fly to avoid memory overhead[cite: 8, 235].
 * [cite_start]The total number of permutations is $9^5$ (59049)[cite: 226].
 */
public class PermutationIterator implements Iterator<int[]> {
    private static final int COUNT = 5; // Exactly 5 remaining empty cells [cite: 224]
    private static final int MAX_VALUE = 9; // 9 possible values (1-9) for each cell [cite: 225]
    
    private final int[] currentCombination; // Holds the current permutation, e.g., [1, 1, 1, 1, 1]
    private boolean hasNext = true;
    
    public PermutationIterator() {
        this.currentCombination = new int[COUNT];
        // Start at [1, 1, 1, 1, 1]
        Arrays.fill(currentCombination, 1);
    }
    
    @Override
    public boolean hasNext() {
        return hasNext;
    }
    
    @Override
    public int[] next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }

        // 1. Get the current combination to return
        int[] result = Arrays.copyOf(currentCombination, COUNT);

        // 2. Calculate the next combination
        int i = COUNT - 1;
        while (i >= 0) {
            if (currentCombination[i] < MAX_VALUE) {
                currentCombination[i]++;
                break;
            } else {
                currentCombination[i] = 1; // Reset to 1
                i--; // Move to the next digit to the left
            }
        }

        // If the loop finished without a 'break', it means all digits wrapped from 9 to 1,
        // which signals the end of the permutations (e.g., from [9, 9, 9, 9, 9] to [1, 1, 1, 1, 1] and i=-1)
        if (i < 0) {
            hasNext = false;
        }

        return result;
    }
}