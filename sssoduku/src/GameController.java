import java.io.IOException;
import java.util.Map;

/**
 * Implements the Viewable interface and acts as the main application logic/controller.
 */
public class GameController implements Viewable {
    private final StorageService storageService;
    private final Verifier verifier;
    private final GameGenerator gameGenerator;
    private final GameSolver gameSolver;

    public GameController() throws IOException {
        // Initialize all components
        this.storageService = new StorageService();
        this.verifier = new Verifier();
        this.gameGenerator = new GameGenerator(verifier);
        this.gameSolver = new GameSolver(verifier);
    }
    
    @Override
    public Catalog getCatalog() {
        return storageService.checkGames();
    }
    
    @Override
    public Game getGame(DifficultyEnum level) throws NotFoundException {
        try {
            Game loadedGame = storageService.loadGame(level);
            // Save the newly loaded game as the 'current' game
            storageService.saveCurrentGame(loadedGame);
            return loadedGame;
        } catch (IOException e) {
            throw new RuntimeException("Error loading game: " + e.getMessage());
        }
    }
    
    @Override
    public void driveGames(Game source) throws SolutionInvalidException {
        // 1. Verify the source solution [cite: 20]
        // If INVALID or INCOMPLETE, throws an exception [cite: 21]
        verifier.verifySourceSolution(source.getBoard()); 

        // 2. Generate and save the three difficulty levels [cite: 29]
        Map<DifficultyEnum, Game> generatedGames = gameGenerator.generateGames(source);
        
        try {
            storageService.saveGame(DifficultyEnum.EASY, generatedGames.get(DifficultyEnum.EASY)); 
            storageService.saveGame(DifficultyEnum.MEDIUM, generatedGames.get(DifficultyEnum.MEDIUM)); 
            storageService.saveGame(DifficultyEnum.HARD, generatedGames.get(DifficultyEnum.HARD)); 
            // Note: The flow diagram shows an optional step to ask difficulty and load a game here.
            // This is left for the Presentation Layer to handle.
        } catch (IOException e) {
            throw new RuntimeException("Error saving generated games: " + e.getMessage());
        }
    }
    
    @Override
    public String verifyGame(Game game) {
        // Returns a string indicating state and location of invalid duplicates [cite: 6]
        Verifier.State state = verifier.verify(game.getBoard());
        
        if (state == Verifier.State.VALID) {
            // If completed board is VALID, delete the game and log file [cite: 72]
            try {
                storageService.deleteCurrentGame(); 
                // Also delete the game from the original difficulty folder (assuming INCOMPLETE game retains its original level)
                if (game.getDifficulty() != DifficultyEnum.INCOMPLETE) {
                    storageService.deleteGameFromFolder(game.getDifficulty());
                }
            } catch (IOException e) {
                // Log or handle deletion failure
            }
            return "VALID";
        }
        
        if (state == Verifier.State.INCOMPLETE) {
            return "INCOMPLETE";
        }
        
        // If INVALID, return error string with duplicates [cite: 6]
        return verifier.getVerificationResult(game);
    }
    
    @Override
    public int[] solveGame(Game game) throws InvalidGameException {
        // Bounded to 5 missing cells [cite: 224]
        return gameSolver.solve(game);
    }
    
    @Override
    public void logUserAction(String userAction) throws IOException {
        // The Presentation Layer is responsible for providing the action in the correct string format [cite: 6]
        // Example: "(3, 5, 3, 0)"
        
        // Parse the log string to a UserAction object
        String[] parts = userAction.replaceAll("[()\\s]", "").split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int value = Integer.parseInt(parts[2]);
        int previous = Integer.parseInt(parts[3]);
        
        UserAction action = new UserAction(x, y, value, previous);
        
        // Log the action immediately [cite: 212]
        storageService.logUserAction(action);
    }
    
    // Additional Method: This is an application-level method for the View to call after a move
    public void processMove(Game currentGame, int row, int col, int value) throws IOException {
        // Get the previous value before updating the board
        int previousValue = currentGame.setCellValue(row, col, value);
        
        // Log the move. [cite_start]The log format is (x, y, val, prev) [cite: 215]
        UserAction action = new UserAction(row, col, value, previousValue);
        logUserAction(action.toString());
        
        // Update the persistent game file [cite: 137]
        storageService.saveCurrentGame(currentGame);
    }
    
    // Additional Method: For Undo functionality
    public void undo(Game currentGame) throws IOException {
        storageService.undoLastMove(currentGame); 
    }
}