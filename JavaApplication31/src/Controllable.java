import java.io.IOException;

public interface Controllable {

    boolean[] getCatalog();

    int[][] getGame(char level) throws Exception;

    void driveGames(String sourcePath) throws Exception;

    boolean[][] verifyGame(int[][] game);

    GameState verifyState(int[][] game);

    int[][] solveGame(int[][] game) throws Exception;

    void logUserAction(UserAction userAction) throws IOException;

    int[][] getSolution();  

    int[][] getCurrentGame();

    boolean[][] getIsOriginal();

    void saveGame() throws IOException;

    void loadGame() throws IOException, ClassNotFoundException;

    void deleteCurrentGame() throws IOException;

    int getRemainingCells();

    int getFaults();

    void setFaults(int faults);
}
