
import java.io.IOException;
public interface Viewable {
    // Returns the game catalog status [cite: 6]
    Catalog getCatalog();

    // Returns a random game with the specified difficulty [cite: 6]
    Game getGame(DifficultyEnum level) throws NotFoundException;

  // Gets a sourceSolution and generates three levels of difficulty [cite: 6]
    void driveGames(Game source) throws SolutionInvalidException;

    // Given a game, if invalid returns invalid and the locates the invalid duplicates [cite: 6]
    // if valid and complete, return a value [cite: 6]
   // if valid and incomplete, returns another value [cite: 6]
    String verifyGame(Game game);

    // Returns the correct combination for the missing numbers [cite: 6]
    int[] solveGame(Game game) throws InvalidGameException;

    // Logs the user action [cite: 6]
    void logUserAction(String userAction) throws IOException;
}
