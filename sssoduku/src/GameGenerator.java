import java.util.*;

/**public class GameGenerator {
    private final Verifier verifier;
    private final Random random;

    public GameGenerator(Verifier verifier) {
        this.verifier = verifier;
        this.random = new Random();
    }

    public Map<DifficultyEnum, Game> generateGames(Game source) {
        Map<DifficultyEnum, Game> games = new HashMap<>();
        
        try {
            // Verify the source board
            verifier.verifySourceSolution(source.getBoard());
            System.out.println("Source board is valid");
            
            // Generate games with different difficulty levels
            Game hardGame = generateLevel(new Game(source), 25, DifficultyEnum.HARD);
            games.put(DifficultyEnum.HARD, hardGame);
            System.out.println("Hard level - Removed " + countRemovedCells(source, hardGame) + " cells");

            Game mediumGame = generateLevel(new Game(source), 20, DifficultyEnum.MEDIUM);
            games.put(DifficultyEnum.MEDIUM, mediumGame);
            System.out.println("Medium level - Removed " + countRemovedCells(source, mediumGame) + " cells");
            
            Game easyGame = generateLevel(new Game(source), 10, DifficultyEnum.EASY);
            games.put(DifficultyEnum.EASY, easyGame);
            System.out.println("Easy level - Removed " + countRemovedCells(source, easyGame) + " cells");
            
        } catch (SolutionInvalidException e) {
            System.err.println("Error: Source board is not a valid Sudoku solution");
            System.err.println(e.getMessage());
            // Return empty map if source is invalid
            return new HashMap<>();
        } catch (Exception e) {
            System.err.println("Unexpected error during game generation: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
        
        return games;
    }

    private Game generateLevel(Game game, int cellsToRemove, DifficultyEnum level) {
        int[][] board = new int[9][9];
        
        // Create a deep copy of the board
        for (int i = 0; i < 9; i++) {
            System.arraycopy(game.getBoard()[i], 0, board[i], 0, 9);
        }
        
        // Get all non-zero positions
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != 0) {
                    positions.add(i * 9 + j);
                }
            }
        }
        
        // Ensure we don't remove more cells than available
        cellsToRemove = Math.min(cellsToRemove, positions.size());
        
        // Shuffle the positions
        Collections.shuffle(positions, random);
        
        // Remove the cells
        for (int i = 0; i < cellsToRemove; i++) {
            int pos = positions.get(i);
            int row = pos / 9;
            int col = pos % 9;
            board[row][col] = 0;
        }
        
        return new Game(board, level);
    }
    
    private int countRemovedCells(Game source, Game modified) {
        int removed = 0;
        int[][] sourceBoard = source.getBoard();
        int[][] modifiedBoard = modified.getBoard();
        
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (sourceBoard[i][j] != 0 && modifiedBoard[i][j] == 0) {
                    removed++;
                }
            }
        }
        return removed;
    }
}**/
public class GameGenerator {
    private final Verifier verifier;
    private final Random random;

    public GameGenerator(Verifier verifier) {
        this.verifier = verifier;
        this.random = new Random();
    }

    public Map<DifficultyEnum, Game> generateGames(Game source) {
        Map<DifficultyEnum, Game> games = new HashMap<>();

        try {
            // Verify the source board
            verifier.verifySourceSolution(source.getBoard());
            System.out.println("Source board is valid");

            // Generate games with different difficulty levels
            Game hardGame = generateLevel(new Game(source), 25, DifficultyEnum.HARD);
            games.put(DifficultyEnum.HARD, hardGame);
            System.out.println("Hard level - Removed " + countRemovedCells(source, hardGame) + " cells");

            Game mediumGame = generateLevel(new Game(source), 20, DifficultyEnum.MEDIUM);
            games.put(DifficultyEnum.MEDIUM, mediumGame);
            System.out.println("Medium level - Removed " + countRemovedCells(source, mediumGame) + " cells");

            Game easyGame = generateLevel(new Game(source), 10, DifficultyEnum.EASY);
            games.put(DifficultyEnum.EASY, easyGame);
            System.out.println("Easy level - Removed " + countRemovedCells(source, easyGame) + " cells");

        } catch (SolutionInvalidException e) {
            System.err.println("Error: Source board is not a valid Sudoku solution");
            System.err.println(e.getMessage());
            return new HashMap<>();
        } catch (Exception e) {
            System.err.println("Unexpected error during game generation: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }

        return games;
    }

    private Game generateLevel(Game game, int cellsToRemove, DifficultyEnum level) {
        int[][] board = game.getBoard();

        // Get all non-zero positions
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != 0) {
                    positions.add(i * 9 + j);
                }
            }
        }

        // Ensure we don't remove more cells than available
        cellsToRemove = Math.min(cellsToRemove, positions.size());

        // Shuffle the positions
        Collections.shuffle(positions, random);

        // Remove the cells
        for (int i = 0; i < cellsToRemove; i++) {
            int pos = positions.get(i);
            int row = pos / 9;
            int col = pos % 9;
            board[row][col] = 0;
        }

        return new Game(board, level);
    }

    private int countRemovedCells(Game source, Game modified) {
        int removed = 0;
        int[][] sourceBoard = source.getBoard();
        int[][] modifiedBoard = modified.getBoard();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (sourceBoard[i][j] != 0 && modifiedBoard[i][j] == 0) {
                    removed++;
                }
            }
        }
        return removed;
    }
}