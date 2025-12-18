import java.io.IOException;
public interface Controllable {
    Catalog getCatalog();

    int[][] getGame(char level) throws NotFoundException;

    void driveGames(int[][] source) throws SolutionInvalidException;

    // A boolean array which says if a specific cell is correct or invalid [cite: 7]
    boolean[][] verifyGame(int[][] game);

    // contains the cell x, y and solution for each missing cell [cite: 7]
    int[][] solveGame(int[][] game) throws InvalidGameException;

    // Logs the user action [cite: 7]
    void logUserAction(UserAction userAction) throws IOException;
}
