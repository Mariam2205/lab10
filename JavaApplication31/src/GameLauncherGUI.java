import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GameLauncherGUI extends JFrame {
    private static final String CURRENT_SAVE_FILE = "sudoku_games" + File.separator + "incomplete" + File.separator + "game.txt";
    private JLabel statusLabel;
    private JButton continueButton;
    
    public GameLauncherGUI() {
        setTitle("Sudoku Game Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        continueButton = new JButton("Continue Saved Game");
        JButton newEasyButton = new JButton("New Easy Game");
        JButton newMediumButton = new JButton("New Medium Game");
        JButton newHardButton = new JButton("New Hard Game");
        JButton exitButton = new JButton("Exit");
        
        
        styleButton(continueButton, new Color(70, 130, 180));
        styleButton(newEasyButton, new Color(100, 200, 100));
        styleButton(newMediumButton, new Color(255, 200, 100));
        styleButton(newHardButton, new Color(220, 100, 100));
        styleButton(exitButton, new Color(120, 120, 120));
        
        
        continueButton.addActionListener(e -> loadGame());
        newEasyButton.addActionListener(e -> startNewGame('E'));
        newMediumButton.addActionListener(e -> startNewGame('M'));
        newHardButton.addActionListener(e -> startNewGame('H'));
        exitButton.addActionListener(e -> System.exit(0));
        
        
        buttonPanel.add(continueButton);
        buttonPanel.add(newEasyButton);
        buttonPanel.add(newMediumButton);
        buttonPanel.add(newHardButton);
        buttonPanel.add(exitButton);
        
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        
        checkForSavedGame();
        
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