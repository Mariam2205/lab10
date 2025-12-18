import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;  // Add this import


// This class represents the actual GUI (The View)
public class SudokuGUI extends JFrame {
    // The link to the Controller layer
    private final Controllable gameController;
    private int[][] currentBoard;
    private char currentDifficulty; // 'E', 'M', 'H', or 'I'
    private JPanel boardPanel;
    private int mistakeCount = 0;
    private static final int MAX_MISTAKES = 3;
    private JButton[][] cellButtons;
    private boolean[][] fixedCells; // Tracks which cells are part of the original puzzle
    private Stack<UserAction> moveHistory = new Stack<>();
    private boolean isUndoing = false;
    public SudokuGUI() throws IOException {
        super("Sudoku Game");
        this.gameController = new PresentationLayerMock();
        
        // Set up the main window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLayout(new BorderLayout());

        // Create game board panel
        boardPanel = new JPanel(new GridLayout(9, 9, 1, 1));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cellButtons = new JButton[9][9];
        fixedCells = new boolean[9][9];

        // Initialize the board cells
        initializeBoard();

        // Add components to the frame
        add(boardPanel, BorderLayout.CENTER);

        // Add control buttons panel
        JPanel controlPanel = new JPanel();
        JButton verifyButton = new JButton("Verify");
        JButton solveButton = new JButton("Solve");
        JButton newGameButton = new JButton("New Game");
        JButton undoButton = new JButton("Undo");
        verifyButton.addActionListener(e -> verifyButtonClicked());
        solveButton.addActionListener(e -> solveButtonClicked());
        newGameButton.addActionListener(e -> handleStartup());
        undoButton.addActionListener(e -> undoLastMove());
        controlPanel.add(verifyButton);
        controlPanel.add(solveButton);
        controlPanel.add(newGameButton);
        controlPanel.add(undoButton);
        add(controlPanel, BorderLayout.SOUTH);

        // Start the application
        handleStartup();
        setVisible(true);
    }

    private void initializeBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JButton button = new JButton();
                button.setFont(new Font("Arial", Font.PLAIN, 20));
                button.setBackground(Color.WHITE);
                button.setOpaque(true);
                button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                final int row = i;
                final int col = j;
                button.addActionListener(e -> cellClicked(row, col));
                cellButtons[i][j] = button;
                boardPanel.add(button);
            }
        }
    }

    private void cellClicked(int row, int col) {
        // Don't allow modifying fixed cells
        if (fixedCells[row][col]) {
            showError("This cell is part of the puzzle and cannot be changed");
            return;
        }

        String value = JOptionPane.showInputDialog(this, "Enter number (1-9) or 0 to clear:");
        try {
            int num = Integer.parseInt(value);
            if (num >= 0 && num <= 9) {
                makeMove(row, col, num);
                updateBoard();
            } else {
                showError("Please enter a number between 1 and 9, or 0 to clear");
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number");
        }
    }

    private void updateBoard() {
        if (currentBoard == null) return;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JButton button = cellButtons[i][j];
                int value = currentBoard[i][j];
                button.setText(value == 0 ? "" : String.valueOf(value));
                
                // Style fixed cells differently
                if (fixedCells[i][j]) {
                    button.setFont(button.getFont().deriveFont(Font.BOLD));
                    button.setBackground(new Color(240, 240, 240));
                    button.setEnabled(false);
                } else {
                    button.setFont(button.getFont().deriveFont(Font.PLAIN));
                    button.setBackground(Color.WHITE);
                    button.setEnabled(true);
                }
            }
        }
    }

    private void handleStartup() {
        moveHistory.clear();
        mistakeCount = 0;
        Catalog catalog = gameController.getCatalog();
        
        // Check if there's a game in progress
        if (catalog.current) {
            int response = JOptionPane.showConfirmDialog(
                this,
                "Would you like to continue your last game?",
                "Continue Game",
                JOptionPane.YES_NO_OPTION
            );
            
            if (response == JOptionPane.YES_OPTION) {
                loadGame('I'); // 'I' for Incomplete game
                return;
            }
        }
        
        // If no game to continue or user chose not to, start a new game
        char choice = askUserForDifficulty();
        loadGame(choice);
    }
private void makeMove(int row, int col, int newValue) {
    if (fixedCells[row][col] || isUndoing) {
        return;
    }
    
    int previousValue = currentBoard[row][col];
    if (previousValue == newValue) {
        return; // No change
    }

    // Save the move to history before making it
    if (newValue != 0) { // Only save non-zero moves
        moveHistory.push(new UserAction(row, col, newValue, previousValue));
    }

    // Check if the move is valid
    int[][] tempBoard = copyBoard(currentBoard);
    tempBoard[row][col] = newValue;
    boolean isValid = isMoveValid(tempBoard, row, col, newValue);

    if (!isValid) {
        mistakeCount++;
        showError("Incorrect move! Mistakes: " + mistakeCount + "/" + MAX_MISTAKES);
        
        // Flash the incorrect cell in red
        flashCell(row, col, Color.RED);
        
        if (mistakeCount >= MAX_MISTAKES) {
            gameOver();
            return;
        }
    } else {
        // Reset mistake count on correct move
        mistakeCount = 0;
    }

    // Update the board
    currentBoard[row][col] = newValue;
    
    // Log the move
    UserAction action = new UserAction(row, col, newValue, previousValue);
    try {
        gameController.logUserAction(action);
    } catch (IOException e) {
        showError("Error saving move: " + e.getMessage());
    }
    
    updateBoard();
    
    // Check if the game is won
    if (isGameWon()) {
        JOptionPane.showMessageDialog(
            this,
            "Congratulations! You've solved the puzzle!",
            "Puzzle Solved!",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
 private void undoLastMove() {
    if (moveHistory.isEmpty()) {
        showError("No moves to undo");
        return;
    }

    isUndoing = true; // Prevent the undo action from being added to history
    try {
        UserAction lastMove = moveHistory.pop();
        
        // Restore the previous value using x,y coordinates
        currentBoard[lastMove.x][lastMove.y] = lastMove.previousValue;
        
        // Log the undo action
        try {
            gameController.logUserAction(new UserAction(
                lastMove.x,
                lastMove.y,
                lastMove.previousValue,
                lastMove.value
            ));
        } catch (IOException e) {
            showError("Error logging undo: " + e.getMessage());
        }
        
        updateBoard();
    } finally {
        isUndoing = false;
    }
}
    private boolean isMoveValid(int[][] board, int row, int col, int num) {
    // Check row
    for (int x = 0; x < 9; x++) {
        if (x != col && board[row][x] == num) {
            return false;
        }
    }
    
    // Check column
    for (int x = 0; x < 9; x++) {
        if (x != row && board[x][col] == num) {
            return false;
        }
    }
    
    // Check 3x3 box
    int boxRow = row - row % 3;
    int boxCol = col - col % 3;
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            if ((boxRow + i != row || boxCol + j != col) && 
                board[boxRow + i][boxCol + j] == num) {
                return false;
            }
        }
    }
    
    return true;
}

private boolean isGameWon() {
    // Check if all cells are filled and valid
    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9; j++) {
            if (currentBoard[i][j] == 0 || !isMoveValid(currentBoard, i, j, currentBoard[i][j])) {
                return false;
            }
        }
    }
    return true;
}

private int[][] copyBoard(int[][] original) {
    int[][] copy = new int[9][9];
    for (int i = 0; i < 9; i++) {
        System.arraycopy(original[i], 0, copy[i], 0, 9);
    }
    return copy;
}

private void flashCell(int row, int col, Color color) {
    JButton button = cellButtons[row][col];
    Color originalColor = button.getBackground();
    button.setBackground(color);
    
    // Reset color after 500ms
    Timer timer = new Timer(500, e -> {
        button.setBackground(originalColor);
    });
    timer.setRepeats(false);
    timer.start();
}

private void gameOver() {
    JOptionPane.showMessageDialog(
        this,
        "Game Over! You've made too many mistakes.",
        "Game Over",
        JOptionPane.ERROR_MESSAGE
    );
    
    // Disable all cells
    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9; j++) {
            cellButtons[i][j].setEnabled(false);
        }
    }
    
    // Disable control buttons
    for (Component comp : ((JPanel)getContentPane().getComponent(1)).getComponents()) {
        if (comp instanceof JButton) {
            JButton button = (JButton) comp;
            if (!button.getText().equals("New Game")) {
                button.setEnabled(false);
            }
        }
    }
}

    public void verifyButtonClicked() {
        boolean[][] status = gameController.verifyGame(currentBoard);
        highlightCells(status);
    }

    public void solveButtonClicked() {
        try {
            int[][] solution = gameController.solveGame(currentBoard);
            applySolutionToBoard(solution);
            verifyButtonClicked();
        } catch (InvalidGameException e) {
            showError("Cannot solve: " + e.getMessage());
        }
    }

    public void undoButtonClicked() {
        loadGame(currentDifficulty);
    }

    private void loadGame(char level) {
        try {
            currentBoard = gameController.getGame(level);
            currentDifficulty = level;
            
            // Initialize fixed cells (non-zero cells are fixed)
            fixedCells = new boolean[9][9];
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    fixedCells[i][j] = (currentBoard[i][j] != 0);
                }
            }
            
            updateBoard();
        } catch (NotFoundException e) {
            showError("Could not load game: " + e.getMessage());
        }
    }

    private void generateNewGames(String solvedPath) {
        if (solvedPath == null) {
            showError("No file selected. Please select a valid Sudoku solution file.");
            return;
        }

        int[][] solvedBoard = readBoardFromFile(new File(solvedPath));
        if (solvedBoard == null) {
            showError("Could not read the Sudoku board from file. Please check the file format.");
            return;
        }

        try {
            gameController.driveGames(solvedBoard);
            char choice = askUserForDifficulty();
            loadGame(choice);
        } catch (SolutionInvalidException e) {
            showError("The provided solution is invalid or incomplete: " + e.getMessage());
        }
    }

    private char askUserForDifficulty() {
        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Select difficulty level:",
            "Difficulty",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]  // Default to Medium
        );
        
        switch (choice) {
            case 0: return 'E';  // Easy
            case 2: return 'H';  // Hard
            default: return 'M'; // Default to Medium
        }
    }

    private String askUserForSolvedPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a solved Sudoku file");
        int userSelection = fileChooser.showOpenDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private int[][] readBoardFromFile(File file) {
        int[][] board = new int[9][9];
        try (Scanner scanner = new Scanner(file)) {
            for (int i = 0; i < 9; i++) {
                String line = scanner.nextLine().trim();
                String[] values = line.split("\\s+");
                for (int j = 0; j < 9 && j < values.length; j++) {
                    try {
                        board[i][j] = Integer.parseInt(values[j]);
                    } catch (NumberFormatException e) {
                        showError("Invalid number format in file at line " + (i+1));
                        return null;
                    }
                }
            }
            return board;
        } catch (Exception e) {
            showError("Error reading file: " + e.getMessage());
            return null;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private void enableSolveButton(boolean enable) {
        // Find the solve button in the control panel
        for (Component comp : ((JPanel)getContentPane().getComponent(1)).getComponents()) {
            if (comp instanceof JButton && ((JButton)comp).getText().equals("Solve")) {
                comp.setEnabled(enable);
                break;
            }
        }
    }

    private void highlightCells(boolean[][] status) {
        if (status == null) return;
        
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JButton button = cellButtons[i][j];
                if (!fixedCells[i][j]) { // Only highlight user-filled cells
                    button.setBackground(status[i][j] ? new Color(200, 255, 200) : new Color(255, 200, 200));
                }
            }
        }
    }

    private void applySolutionToBoard(int[][] solution) {
        if (solution == null) return;
        
        for (int[] move : solution) {
            if (move.length >= 3) {
                int row = move[0];
                int col = move[1];
                int value = move[2];
                if (!fixedCells[row][col]) { // Only apply to non-fixed cells
                    currentBoard[row][col] = value;
                }
            }
        }
        updateBoard();
    }

    private boolean askUserToContinue() {
        return JOptionPane.showConfirmDialog(
            this,
            "Would you like to continue your last game?",
            "Continue Game",
            JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION;
    }
}