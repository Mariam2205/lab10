import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SudokuLauncher {
    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Create and show the Sudoku GUI
                SudokuGUI gui = new SudokuGUI();
                // Create a basic window
                JFrame frame = new JFrame("Sudoku Game");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(600, 600);
                frame.setLocationRelativeTo(null); // Center the window
                
                // You'll need to add your actual GUI components here
                // For now, just add a placeholder
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add(new JLabel("Sudoku Game - GUI Implementation", JLabel.CENTER), BorderLayout.CENTER);
                
                frame.add(panel);
                frame.setVisible(true);
                
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error initializing the game: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}