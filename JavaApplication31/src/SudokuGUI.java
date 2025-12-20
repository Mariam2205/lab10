// File: SudokuGUI.java
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class SudokuGUI extends JFrame {
    private static final Color BG_COLOR = new Color(240, 240, 240);
    private static final Color SELECTED_COLOR = new Color(200, 230, 255);
    private static final Color ERROR_COLOR = new Color(255, 200, 200);
    private static final Color GRID_COLOR = new Color(0, 0, 0);
    
    private JButton[][] cells = new JButton[9][9];
    private int[][] solution;
    private int[][] puzzle;
    private boolean[][] isOriginal;
    private Controllable controller;
    private JPanel boardPanel;
    private JButton checkButton;
    private JButton solveButton;
    private JButton newGameButton;
    private JLabel faultsLabel;
    private Point selectedCell = null;
    private int faults = 0;
    private boolean gameOver = false;
    
    public SudokuGUI(Controllable controller) {
        this.controller = controller;
        initializeGame();
        initializeUI();
    }
    
    private void initializeGame() {
        try {
            puzzle = controller.getGame('M'); // Default to medium
            solution = controller.getSolution();
            isOriginal = new boolean[9][9];
            
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    isOriginal[i][j] = (puzzle[i][j] != 0);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error initializing game: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initializeUI() {
        setTitle("Sudoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Create the game board
        boardPanel = new JPanel(new GridLayout(3, 3, 2, 2));
        boardPanel.setBackground(GRID_COLOR);
        boardPanel.setBorder(BorderFactory.createLineBorder(GRID_COLOR, 2));
        
        // Create 3x3 subgrids
        JPanel[][] subgrids = new JPanel[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                subgrids[i][j] = new JPanel(new GridLayout(3, 3, 1, 1));
                subgrids[i][j].setBackground(GRID_COLOR);
                subgrids[i][j].setBorder(BorderFactory.createLineBorder(GRID_COLOR, 1));
                boardPanel.add(subgrids[i][j]);
            }
        }
        
        // Create cells and add them to subgrids
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                final int row = i;
                final int col = j;
                
                cells[i][j] = new JButton();
                cells[i][j].setFont(new Font("Arial", Font.BOLD, 20));
                cells[i][j].setFocusPainted(false);
                cells[i][j].setBackground(BG_COLOR);
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                
                // Set initial values
                if (puzzle[i][j] != 0) {
                    cells[i][j].setText(String.valueOf(puzzle[i][j]));
                    cells[i][j].setForeground(Color.BLACK);
                    cells[i][j].setEnabled(false);
                } else {
                    cells[i][j].setText("");
                    cells[i][j].addActionListener(e -> selectCell(row, col));
                    
                    // Add key listener for number input
                    cells[i][j].addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            if (selectedCell == null || gameOver) return;
                            
                            int keyCode = e.getKeyCode();
                            
                            // Handle number keys
                            if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9) {
                                setCellValue(selectedCell.x, selectedCell.y, keyCode - KeyEvent.VK_0);
                            } 
                            // Handle numpad numbers
                            else if (keyCode >= KeyEvent.VK_NUMPAD1 && keyCode <= KeyEvent.VK_NUMPAD9) {
                                setCellValue(selectedCell.x, selectedCell.y, keyCode - KeyEvent.VK_NUMPAD0);
                            } 
                            // Handle backspace/delete
                            else if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
                                setCellValue(selectedCell.x, selectedCell.y, 0);
                            }
                            // Handle arrow keys for navigation
                            else if (keyCode == KeyEvent.VK_UP && selectedCell.x > 0) {
                                selectCell(selectedCell.x - 1, selectedCell.y);
                            } else if (keyCode == KeyEvent.VK_DOWN && selectedCell.x < 8) {
                                selectCell(selectedCell.x + 1, selectedCell.y);
                            } else if (keyCode == KeyEvent.VK_LEFT && selectedCell.y > 0) {
                                selectCell(selectedCell.x, selectedCell.y - 1);
                            } else if (keyCode == KeyEvent.VK_RIGHT && selectedCell.y < 8) {
                                selectCell(selectedCell.x, selectedCell.y + 1);
                            }
                        }
                    });
                }
                
                // Add to appropriate subgrid
                subgrids[i/3][j/3].add(cells[i][j]);
            }
        }
        
        // Create control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Game info panel
        JPanel infoPanel = new JPanel();
        faultsLabel = new JLabel("Faults: 0/3");
        faultsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        faultsLabel.setForeground(Color.RED);
        infoPanel.add(faultsLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        checkButton = new JButton("Check Solution");
        solveButton = new JButton("Solve");
        newGameButton = new JButton("New Game");
        
        styleButton(checkButton, new Color(100, 200, 100));
        styleButton(solveButton, new Color(100, 150, 255));
        styleButton(newGameButton, new Color(255, 150, 100));
        
        checkButton.addActionListener(e -> checkSolution());
        solveButton.addActionListener(e -> solvePuzzle());
        newGameButton.addActionListener(e -> {
            this.dispose();
            new GameLauncherGUI().setVisible(true);
        });
        
        // Initially hide solve button
        solveButton.setVisible(false);
        
        buttonPanel.add(checkButton);
        buttonPanel.add(solveButton);
        buttonPanel.add(newGameButton);
        
        // Add components to control panel
        controlPanel.add(infoPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(buttonPanel);
        
        // Add components to frame
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        // Set window properties
        pack();
        setMinimumSize(new Dimension(500, 550));
        setLocationRelativeTo(null);
    }
    
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }
    
    private void selectCell(int row, int col) {
        if (gameOver) return;
        
        // Deselect previous cell
        if (selectedCell != null) {
            int prevRow = selectedCell.x;
            int prevCol = selectedCell.y;
            cells[prevRow][prevCol].setBackground(
                isOriginal[prevRow][prevCol] ? BG_COLOR : 
                (puzzle[prevRow][prevCol] == 0 ? BG_COLOR : 
                (puzzle[prevRow][prevCol] == solution[prevRow][prevCol] ? 
                    new Color(200, 255, 200) : new Color(255, 200, 200)))
            );
        }
        
        // Select new cell
        selectedCell = new Point(row, col);
        cells[row][col].setBackground(SELECTED_COLOR);
        cells[row][col].requestFocusInWindow();
    }
    
    private void setCellValue(int row, int col, int value) {
        if (gameOver || isOriginal[row][col]) {
            return;
        }
        
        puzzle[row][col] = value;
        if (value == 0) {
            cells[row][col].setText("");
            cells[row][col].setBackground(SELECTED_COLOR);
        } else {
            cells[row][col].setText(String.valueOf(value));
            boolean isCorrect = (value == solution[row][col]);
            cells[row][col].setForeground(isCorrect ? new Color(0, 100, 0) : Color.RED);
            
            if (!isCorrect) {
                faults++;
                faultsLabel.setText("Faults: " + faults + "/3");
                
                if (faults >= 3) {
                    gameOver = true;
                    JOptionPane.showMessageDialog(this,
                        "Game Over! You've made 3 mistakes.",
                        "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
                    solveButton.setVisible(true);
                    return;
                }
            }
        }
        
        // Check if solve button should be shown (5 or fewer cells remaining)
        int remaining = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (puzzle[i][j] == 0) {
                    remaining++;
                }
            }
        }
        solveButton.setVisible(remaining <= 5);
        
        // Check if puzzle is complete
        if (remaining == 0) {
            checkSolution();
        }
    }
    
    private void checkSolution() {
        boolean isComplete = true;
        boolean isCorrect = true;
        
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (puzzle[i][j] == 0) {
                    isComplete = false;
                    isCorrect = false;
                    break;
                } else if (puzzle[i][j] != solution[i][j]) {
                    isCorrect = false;
                }
            }
            if (!isComplete) break;
        }
        
        if (!isComplete) {
            JOptionPane.showMessageDialog(this,
                "The puzzle is not complete yet!",
                "Incomplete",
                JOptionPane.INFORMATION_MESSAGE);
        } else if (isCorrect) {
            JOptionPane.showMessageDialog(this,
                "Congratulations! You've solved the puzzle!",
                "Puzzle Solved!",
                JOptionPane.INFORMATION_MESSAGE);
            gameOver = true;
            solveButton.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this,
                "There are some incorrect numbers in the puzzle.",
                "Incorrect Solution",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void solvePuzzle() {
        int response = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to see the solution?",
            "Solve Puzzle",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (!isOriginal[i][j]) {
                        puzzle[i][j] = solution[i][j];
                        cells[i][j].setText(String.valueOf(solution[i][j]));
                        cells[i][j].setForeground(new Color(0, 100, 0));
                    }
                }
            }
            gameOver = true;
            solveButton.setVisible(false);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // For testing without launcher
            // Controller controller = new Controller();
            // controller.getGame('M'); // Default to medium
            // new SudokuGUI(controller).setVisible(true);
            
            new GameLauncherGUI().setVisible(true);
        });
    }
}