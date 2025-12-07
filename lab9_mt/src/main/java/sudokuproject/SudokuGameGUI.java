package sudokuproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SudokuGameGUI extends JFrame {
    private final SudokuModel model;
    private final CellPanel[][] cells = new CellPanel[9][9];

    private final JLabel scoreLabel = new JLabel();
    private final JLabel messageLabel = new JLabel(" ");
    private final JToggleButton pencilToggle = new JToggleButton("Pencil");

    public SudokuGameGUI() {
        super("Sudoku Game");
        model = new SudokuModel();
        initUI();
        newGame();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(820, 920);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        JPanel controls = new JPanel();

        JButton newBtn = new JButton("New Game");
        newBtn.addActionListener(e -> newGame());
        controls.add(newBtn);

        JButton hintBtn = new JButton("Hint");
        hintBtn.addActionListener(e -> doHint());
        controls.add(hintBtn);

        JButton solveBtn = new JButton("Show Solution");
        solveBtn.addActionListener(e -> showSolution());
        controls.add(solveBtn);

        controls.add(pencilToggle);

        scoreLabel.setText("Score: " + model.getScore());
        controls.add(scoreLabel);

        top.add(controls, BorderLayout.CENTER);
        top.add(messageLabel, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(9, 9));
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                CellPanel cp = new CellPanel(r, c);
                cells[r][c] = cp;
                grid.add(cp);
            }
        }
        add(grid, BorderLayout.CENTER);

        JTextArea instructions = new JTextArea(
                "Instructions:\n- Toggle Pencil to make notes (won't commit).\n- Type digits 1-9 to place numbers.\n- Wrong placements in Pen mode lose 1 point and are rejected.\n- Hint reveals a correct cell (cost 5 points).\n- If score <= 0 you lose and the solution appears.\n");
        instructions.setEditable(false);
        instructions.setBackground(getBackground());
        add(instructions, BorderLayout.SOUTH);
    }

    private void newGame() {
        model.generateNewPuzzle(36);
        messageLabel.setText("New game generated. Good luck!");
        scoreLabel.setText("Score: " + model.getScore());
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) cells[r][c].setGiven(model.getGiven(r, c));
    }

    private void doHint() {
        if (model.getScore() < 5) {
            messageLabel.setText("Not enough points for a hint (cost 5)");
            return;
        }
        boolean ok = model.revealOneCell();
        if (!ok) { messageLabel.setText("No hidden cells left."); return; }
        scoreLabel.setText("Score: " + model.getScore());
        refreshAllCells();
        messageLabel.setText("Hint used (-5 points)");
        checkWinOrLose();
    }

    private void showSolution() {
        model.revealSolution();
        refreshAllCells();
        messageLabel.setText("Solution revealed.");
        scoreLabel.setText("Score: " + model.getScore());
    }

    private void refreshAllCells() {
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) cells[r][c].updateFromModel();
    }

    private void checkWinOrLose() {
        if (model.isSolved()) messageLabel.setText("Congratulations! You solved the puzzle.");
        else if (model.getScore() <= 0) {
            messageLabel.setText("You lost (score <= 0). Revealing solution...");
            model.revealSolution(); refreshAllCells();
        }
    }

    private class CellPanel extends JPanel {
        private final int row, col;
        private final JTextField field = new JTextField();
        private final JLabel pencilLabel = new JLabel("", SwingConstants.CENTER);

        public CellPanel(int r, int c) {
            this.row = r; this.col = c;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            field.setHorizontalAlignment(JTextField.CENTER);
            field.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            pencilLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            add(field, BorderLayout.CENTER);
            add(pencilLabel, BorderLayout.SOUTH);

            field.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char ch = e.getKeyChar();
                    if (ch >= '1' && ch <= '9') {
                        int v = ch - '0';
                        if (pencilToggle.isSelected()) {
                            model.togglePencil(row, col, v);
                            updateFromModel();
                        } else {
                            boolean ok = model.placeNumber(row, col, v);
                            if (!ok) {
                                messageLabel.setText("Wrong placement! -1 point");
                                scoreLabel.setText("Score: " + model.getScore());
                                field.setBackground(Color.PINK);
                                javax.swing.Timer t = new javax.swing.Timer(300, ev -> field.setBackground(Color.WHITE));
                                t.setRepeats(false); t.start();
                            } else {
                                messageLabel.setText("Placed " + v);
                                scoreLabel.setText("Score: " + model.getScore());
                            }
                            updateFromModel();
                            checkWinOrLose();
                        }
                        e.consume();
                    } else if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE || e.getKeyChar() == KeyEvent.VK_DELETE) {
                        model.clearCell(row, col);
                        updateFromModel();
                        e.consume();
                    } else {
                        e.consume();
                    }
                }
            });

            setPreferredSize(new Dimension(60, 60));
        }

        public void setGiven(int v) {
            model.setGivenCell(row, col, v);
            updateFromModel();
        }

        public void updateFromModel() {
            if (model.isGiven(row, col)) {
                field.setText(String.valueOf(model.getGiven(row, col)));
                field.setEditable(false);
                field.setForeground(Color.BLUE);
                pencilLabel.setText("");
            } else {
                int val = model.getValue(row, col);
                if (val != 0) {
                    field.setText(String.valueOf(val));
                    field.setEditable(true);
                    field.setForeground(Color.BLACK);
                    pencilLabel.setText("");
                } else {
                    field.setText("");
                    field.setEditable(true);
                    Set<Integer> notes = model.getPencil(row, col);
                    if (notes == null || notes.isEmpty()) pencilLabel.setText("");
                    else pencilLabel.setText(notes.toString());
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SudokuGameGUI());
    }
}
