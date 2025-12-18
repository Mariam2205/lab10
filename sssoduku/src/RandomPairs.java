import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomPairs {
    // Range 0..80 for both x and y (used for a flat array of 81 cells)
    private static final int MAX_COORD = 80; 
    private static final int MAX_UNIQUE_PAIRS = (MAX_COORD + 1) * (MAX_COORD + 1);
    private final Random random; 

    public RandomPairs() {
        // Initializes with a default seed (often based on current time) [cite: 28, 40]
        this.random = new Random(); 
    }

    /**
     * Generate n distinct random pairs (x, y) where $0 \le x \le 80$ and $0 \le y \le 80$.
     */
    public List<int[]> generateDistinctPairs(int n) {
        if (n < 0 || n > MAX_UNIQUE_PAIRS) {
            throw new IllegalArgumentException(
                    "n must be between 0 and " + MAX_UNIQUE_PAIRS + " (inclusive)"); 
        }
        
        Set<Integer> used = new HashSet<>();
        List<int[]> result = new ArrayList<>(n); 
        while (result.size() < n) { 
            int x = random.nextInt(MAX_COORD + 1); 
            int y = random.nextInt(MAX_COORD + 1);
            // Encode pair (x, y) as a single int to track uniqueness 
            int key = x * (MAX_COORD + 1) + y; 
            if (used.add(key)) { 
                
               result.add(new int[] { x, y }); 
            }
        }
        return result;
    }
}