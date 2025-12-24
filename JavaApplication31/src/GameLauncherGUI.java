import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameLauncherGUI extends JFrame {
    private static final String CURRENT_SAVE_FILE = "sudoku_games" + File.separator + "incomplete" + File.separator + "game.txt";
    private static final String SOURCE_FILE = "sudoku_games" + File.separator + "source.txt";
    private JLabel statusLabel;
    private JLabel sourceLabel;
    private JButton continueButton;
    
    public GameLauncherGUI() {
        setTitle("Sudoku Game Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 320);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        sourceLabel = new JLabel("", SwingConstants.CENTER);
        sourceLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        continueButton = new JButton("Continue Saved Game");
        JButton newEasyButton = new JButton("New Easy Game");
        JButton newMediumButton = new JButton("New Medium Game");
        JButton newHardButton = new JButton("New Hard Game");
        JButton chooseSourceButton = new JButton("Choose Source Solution");
        JButton clearSourceButton = new JButton("Clear Source Solution");
        JButton exitButton = new JButton("Exit");
        
        
        styleButton(continueButton, new Color(70, 130, 180));
        styleButton(newEasyButton, new Color(100, 200, 100));
        styleButton(newMediumButton, new Color(255, 200, 100));
        styleButton(newHardButton, new Color(220, 100, 100));
        styleButton(chooseSourceButton, new Color(120, 120, 180));
        styleButton(clearSourceButton, new Color(140, 140, 140));
        styleButton(exitButton, new Color(120, 120, 120));
        
        
        continueButton.addActionListener(e -> loadGame());
        newEasyButton.addActionListener(e -> startNewGame('E'));
        newMediumButton.addActionListener(e -> startNewGame('M'));
        newHardButton.addActionListener(e -> startNewGame('H'));
        chooseSourceButton.addActionListener(e -> {
            try {
                chooseSourceFile();
                refreshSourceStatus();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error selecting source: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        clearSourceButton.addActionListener(e -> {
            try {
                Files.deleteIfExists(Paths.get(SOURCE_FILE));
                refreshSourceStatus();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error clearing source: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        exitButton.addActionListener(e -> System.exit(0));
        
        
        buttonPanel.add(continueButton);
        buttonPanel.add(newEasyButton);
        buttonPanel.add(newMediumButton);
        buttonPanel.add(newHardButton);
        buttonPanel.add(chooseSourceButton);
        buttonPanel.add(clearSourceButton);
        buttonPanel.add(exitButton);
        
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        topPanel.add(statusLabel);
        topPanel.add(sourceLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        
        checkForSavedGame();
        refreshSourceStatus();
        
        add(mainPanel);
    }
    
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
    }
    
    private void checkForSavedGame() {
        File saveFile = new File(CURRENT_SAVE_FILE);
        continueButton.setEnabled(saveFile.exists());
        
        if (saveFile.exists()) {
            statusLabel.setText("A saved game was found!");
            statusLabel.setForeground(new Color(34, 139, 34));
        } else {
            statusLabel.setText("No saved game found. Start a new game!");
            statusLabel.setForeground(new Color(178, 34, 34));
        }
    }
    
    private void loadGame() {
        try {
            Controller controller = new Controller();
            controller.loadGame();
            startGame(controller);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading saved game: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void startNewGame(char difficulty) {
        try {
            Controller controller = new Controller();
            controller.getGame(difficulty);
            startGame(controller);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error starting new game: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void startGame(Controller controller) {
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            SudokuGUI game = new SudokuGUI(controller);
            game.setVisible(true);
        });
    }
    
    private void chooseSourceFile() throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Sudoku source solution (9 lines x 9 numbers)");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();
        if (selected == null) {
            return;
        }

        Path target = Paths.get(SOURCE_FILE);
        Files.createDirectories(target.getParent());
        Files.copy(selected.toPath(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private void refreshSourceStatus() {
        try {
            Path source = Paths.get(SOURCE_FILE);
            if (!Files.exists(source)) {
                sourceLabel.setText("Source: (none) — Please provide a source file");
                sourceLabel.setForeground(new Color(90, 90, 90));
                return;
            }

            int[][] board = readBoard(source);
            GameState state = new Verifier().verifyState(board);
            sourceLabel.setText("Source: " + SOURCE_FILE + " — " + state);
            if (state == GameState.VALID) {
                sourceLabel.setForeground(new Color(34, 139, 34));
            } else if (state == GameState.INCOMPLETE) {
                sourceLabel.setForeground(new Color(255, 140, 0));
            } else {
                sourceLabel.setForeground(new Color(178, 34, 34));
            }
        } catch (Exception e) {
            sourceLabel.setText("Source: " + SOURCE_FILE + " — ERROR");
            sourceLabel.setForeground(new Color(178, 34, 34));
        }
    }

    private static int[][] readBoard(Path file) throws IOException {
        java.util.List<String> lines = Files.readAllLines(file);
        java.util.List<String> filtered = new java.util.ArrayList<>();
        for (String line : lines) {
            if (line != null && !line.isBlank()) {
                filtered.add(line);
            }
            if (filtered.size() >= 9) {
                break;
            }
        }
        if (filtered.size() < 9) {
            throw new IOException("Invalid source board file");
        }

        int[][] board = new int[9][9];
        for (int i = 0; i < 9; i++) {
            String[] nums = filtered.get(i).trim().split("\\s+");
            for (int j = 0; j < 9; j++) {
                board[i][j] = Integer.parseInt(nums[j]);
            }
        }
        return board;
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