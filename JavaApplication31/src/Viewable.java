import java.io.IOException;

public interface Viewable {
    Catalog getCatalog();
    Game getGame(DifficultyEnum level) throws Exception;
    void driveGames(Game sourceGame) throws Exception;
    String verifyGame(Game game);
    int[] solveGame(Game game) throws Exception;
    void logUserAction(String userAction) throws IOException;
}
