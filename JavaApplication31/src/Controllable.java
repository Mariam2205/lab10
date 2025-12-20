
import java.io.IOException;

public interface Controllable {

    boolean[] getCatalog();

    int[][] getGame(char level) throws Exception;

    void driveGames(String sourcePath) throws Exception;

    boolean[][] verifyGame(int[][] game);

    int[][] solveGame(int[][] game) throws Exception;

    void logUserAction(UserAction userAction) throws IOException;

    int[][] getSolution();  // Add this line

    void saveGame() throws IOException;

    void loadGame() throws IOException, ClassNotFoundException;

    int getRemainingCells();

    int getFaults();
}
