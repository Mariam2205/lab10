
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

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
    private JButton undoButton;
    private JButton newGameButton;
    private JLabel faultsLabel;
    private Point selectedCell = null;
    private int faults = 0;
    private boolean gameOver = false;

    private static final String INCOMPLETE_DIR = "sudoku_games" + File.separator + "incomplete";
    private static final String INCOMPLETE_SAVE_FILE = INCOMPLETE_DIR + File.separator + "game.txt";
    private static final String INCOMPLETE_LOG_FILE = INCOMPLETE_DIR + File.separator + "log.txt";
    
    public SudokuGUI(Controllable controller) {
        this.controller = controller;
        initializeGame();
        initializeUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    SudokuGUI.this.controller.setFaults(SudokuGUI.this.faults);
                    SudokuGUI.this.controller.saveGame();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(SudokuGUI.this,
                        "Error saving game: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    private void initializeGame() {
        try {
            puzzle = controller.getCurrentGame();
            if (puzzle == null) {
                puzzle = controller.getGame('M'); 
            }

            solution = controller.getSolution();
            isOriginal = controller.getIsOriginal();

            this.faults = controller.getFaults();
            if (isOriginal == null) {
                isOriginal = new boolean[9][9];
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        isOriginal[i][j] = (puzzle[i][j] != 0);
                    }
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
        
        
        boardPanel = new JPanel(new GridLayout(3, 3, 2, 2));
        boardPanel.setBackground(GRID_COLOR);
        boardPanel.setBorder(BorderFactory.createLineBorder(GRID_COLOR, 2));
        
        
        JPanel[][] subgrids = new JPanel[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                subgrids[i][j] = new JPanel(new GridLayout(3, 3, 1, 1));
                subgrids[i][j].setBackground(GRID_COLOR);
                subgrids[i][j].setBorder(BorderFactory.createLineBorder(GRID_COLOR, 1));
                boardPanel.add(subgrids[i][j]);
            }
        }
        
        
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                final int row = i;
                final int col = j;
                
                cells[i][j] = new JButton();
                cells[i][j].setFont(new Font("Arial", Font.BOLD, 20));
                cells[i][j].setFocusPainted(false);
                cells[i][j].setBackground(BG_COLOR);
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                
                
                if (puzzle[i][j] != 0) {
                    cells[i][j].setText(String.valueOf(puzzle[i][j]));
                    boolean isCorrect = (puzzle[i][j] == solution[i][j]);
                    cells[i][j].setForeground(isCorrect ? new Color(0, 100, 0) : Color.RED);
                } else {
                    cells[i][j].setText("");
                }

                if (isOriginal[i][j]) {
                    cells[i][j].setForeground(Color.BLACK);
                    cells[i][j].setEnabled(false);
                } else {
                    cells[i][j].addActionListener(e -> selectCell(row, col));

                    
                    cells[i][j].addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            if (selectedCell == null || gameOver) return;

                            int keyCode = e.getKeyCode();

                            
                            if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9) {
                                setCellValue(selectedCell.x, selectedCell.y, keyCode - KeyEvent.VK_0);
                            }
                            
                            else if (keyCode >= KeyEvent.VK_NUMPAD1 && keyCode <= KeyEvent.VK_NUMPAD9) {
                                setCellValue(selectedCell.x, selectedCell.y, keyCode - KeyEvent.VK_NUMPAD0);
                            }
                            
                            else if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
                                setCellValue(selectedCell.x, selectedCell.y, 0);
                            }
                            
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
                
                
                subgrids[i/3][j/3].add(cells[i][j]);
            }
        }
        
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        JPanel infoPanel = new JPanel();
        faultsLabel = new JLabel("Faults: " + faults + "/3");
        faultsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        faultsLabel.setForeground(Color.RED);
        infoPanel.add(faultsLabel);
        
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        checkButton = new JButton("Verify");
        solveButton = new JButton("Solve");
        undoButton = new JButton("Undo");
        newGameButton = new JButton("New Game");
        
        styleButton(checkButton, new Color(100, 200, 100));
        styleButton(solveButton, new Color(100, 150, 255));
        styleButton(undoButton, new Color(160, 160, 160));
        styleButton(newGameButton, new Color(255, 150, 100));
        
        checkButton.addActionListener(e -> checkSolution());
        solveButton.addActionListener(e -> solvePuzzle());
        undoButton.addActionListener(e -> undoLastMove());
        newGameButton.addActionListener(e -> {
            try {
                controller.saveGame();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error saving game: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            this.dispose();
            new GameLauncherGUI().setVisible(true);
        });

        solveButton.setVisible(true);
        
        buttonPanel.add(checkButton);
        buttonPanel.add(solveButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(newGameButton);
        
        
        controlPanel.add(infoPanel);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(buttonPanel);
        
        
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        
        pack();
        setMinimumSize(new Dimension(500, 550));
        setLocationRelativeTo(null);

        int remaining = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (puzzle[i][j] == 0) {
                    remaining++;
                }
            }
        }
        solveButton.setEnabled(remaining == 5);
        undoButton.setEnabled(hasUndoEntries());

        
        if (remaining == 0) {
            checkSolution();
        }
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

        
        selectedCell = new Point(row, col);
        cells[row][col].setBackground(SELECTED_COLOR);
        cells[row][col].requestFocusInWindow();
    }

    private void setCellValue(int row, int col, int value) {
        if (gameOver || isOriginal[row][col]) {
            return;
        }

        int prevValue = puzzle[row][col];
        if (prevValue == value) {
            return;
        }

        appendLogEntry(row, col, value, prevValue);
        puzzle[row][col] = value;
        boolean isCorrect = true;
        if (value == 0) {
            cells[row][col].setText("");
            cells[row][col].setBackground(SELECTED_COLOR);
            cells[row][col].setForeground(Color.BLACK);
        } else {
            cells[row][col].setText(String.valueOf(value));
            isCorrect = (value == solution[row][col]);
            cells[row][col].setForeground(isCorrect ? new Color(0, 100, 0) : Color.RED);
        }

        if (!isCorrect) {
            faults++;
            faultsLabel.setText("Faults: " + faults + "/3");

            if (faults >= 3) {
                gameOver = true;
                JOptionPane.showMessageDialog(this,
                    "Game Over! You've made 3 mistakes.",
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        try {
            controller.setFaults(faults);
            controller.saveGame();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error saving game: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }

        
        int remaining = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (puzzle[i][j] == 0) {
                    remaining++;
                }
            }
        }

        solveButton.setEnabled(remaining == 5);
        undoButton.setEnabled(hasUndoEntries());

        
        if (remaining == 0) {
            checkSolution();
        }
    }

    private void checkSolution() {
        GameState state = controller.verifyState(puzzle);
        if (state == GameState.INCOMPLETE) {
            JOptionPane.showMessageDialog(this,
                "Board state: INCOMPLETE",
                "Verify",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (state == GameState.INVALID) {
            JOptionPane.showMessageDialog(this,
                "Board state: INVALID",
                "Verify",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
            "Board state: VALID",
            "Verify",
            JOptionPane.INFORMATION_MESSAGE);

        gameOver = true;
        solveButton.setEnabled(false);
        undoButton.setEnabled(false);

        try {
            controller.deleteCurrentGame();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error deleting completed game: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void solvePuzzle() {
        int remaining = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (puzzle[i][j] == 0) {
                    remaining++;
                }
            }
        }

        if (remaining != 5) {
            JOptionPane.showMessageDialog(this,
                "You can only use Solve when exactly 5 cells are left.",
                "Solve Not Available",
                JOptionPane.INFORMATION_MESSAGE);
            solveButton.setEnabled(false);
            return;
        }

        int response = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to see the solution?",
            "Solve Puzzle",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            try {
                controller.solveGame(puzzle);

                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        if (!isOriginal[i][j]) {
                            if (puzzle[i][j] == 0) {
                                cells[i][j].setText("");
                            } else {
                                cells[i][j].setText(String.valueOf(puzzle[i][j]));
                            }
                            boolean isCorrect = (puzzle[i][j] != 0 && puzzle[i][j] == solution[i][j]);
                            cells[i][j].setForeground(isCorrect ? new Color(0, 100, 0) : Color.RED);
                        }
                    }
                }

                gameOver = true;
                solveButton.setEnabled(false);
                undoButton.setEnabled(false);

                controller.deleteCurrentGame();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error solving puzzle: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean hasUndoEntries() {
        try {
            Path log = Paths.get(INCOMPLETE_LOG_FILE);
            if (!Files.exists(log)) return false;
            List<String> lines = Files.readAllLines(log);
            return !lines.isEmpty();
        } catch (IOException e) {
            return false;
        }
    }

    private void appendLogEntry(int x, int y, int val, int prev) {
        try {
            Files.createDirectories(Paths.get(INCOMPLETE_DIR));

            Path save = Paths.get(INCOMPLETE_SAVE_FILE);
            if (!Files.exists(save)) {
                controller.saveGame();
            }

            String line = "(" + x + ", " + y + ", " + val + ", " + prev + ")" + System.lineSeparator();
            Files.writeString(Paths.get(INCOMPLETE_LOG_FILE), line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error writing log: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void undoLastMove() {
        if (gameOver) return;

        Path log = Paths.get(INCOMPLETE_LOG_FILE);
        if (!Files.exists(log)) {
            undoButton.setEnabled(false);
            return;
        }

        try {
            List<String> lines = Files.readAllLines(log);
            if (lines.isEmpty()) {
                undoButton.setEnabled(false);
                return;
            }

            String last = lines.get(lines.size() - 1).trim();
            String cleaned = last.replace("(", "").replace(")", "");
            String[] parts = cleaned.split(",");
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            int prev = Integer.parseInt(parts[3].trim());

            lines = lines.subList(0, lines.size() - 1);
            Files.write(log, lines);

            if (!isOriginal[x][y]) {
                puzzle[x][y] = prev;
                if (prev == 0) {
                    cells[x][y].setText("");
                } else {
                    cells[x][y].setText(String.valueOf(prev));
                }
                boolean isCorrect = (prev != 0 && prev == solution[x][y]);
                cells[x][y].setForeground(isCorrect ? new Color(0, 100, 0) : Color.RED);
            }

            int remaining = 0;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (puzzle[i][j] == 0) {
                        remaining++;
                    }
                }
            }
            solveButton.setEnabled(remaining == 5);
            undoButton.setEnabled(hasUndoEntries());

            controller.setFaults(faults);
            controller.saveGame();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error undoing move: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            
            
            
            

            new GameLauncherGUI().setVisible(true);
        });
    }
}