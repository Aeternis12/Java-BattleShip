import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Battleship extends JFrame {
    private static final int SIZE = 10;
    private static final int CELL_SIZE = 40;

    private boolean playerOnesTurn = true;
    private boolean turnLocked = false;

    private JPanel board1;
    private JPanel board2;

    private JLabel turnLabel;
    private Timer countdownTimer;
    private int countdown = 3;

    public Battleship() {
        setTitle("Battleship");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2, 40, 0));

        board1 = createBoard(true); // Player 1 clicks here
        board2 = createBoard(false); // Player 2 clicks here 
        mainPanel.add(board1);
        mainPanel.add(board2);

        turnLabel = new JLabel("Player 1's Turn", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Serif", Font.BOLD, 18));

        add(turnLabel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        pack(); // Sizes window based on button sizes
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createBoard(boolean isLeftBoard) {
        JPanel board = new JPanel();
        board.setLayout(new GridLayout(SIZE, SIZE));
        board.setPreferredSize(new Dimension(SIZE * CELL_SIZE, SIZE * CELL_SIZE));

        for (int i = 0; i < SIZE * SIZE; i++) {
            JButton cell = new JButton();
            cell.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            cell.setBackground(new Color(173, 216, 230)); // Light blue
            cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            cell.setOpaque(true);
            cell.setFocusPainted(false);

            cell.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    if (turnLocked) return;

                    // Player 1 can only click LEFT board
                    if (playerOnesTurn && !isLeftBoard) return;

                    // Player 2 can only click RIGHT board
                    if (!playerOnesTurn && isLeftBoard) return;

                    if(!cell.getBackground().equals(Color.WHITE)) {
                        cell.setBackground(Color.WHITE);
                        endTurn();
                    }
                }
            });

            board.add(cell);
        }

        return board;
    }

    private void endTurn() {
        turnLocked = true;
        setBoardsEnabled(false);

        countdown = 3;
        turnLabel.setText("Next turn in: " + countdown);

        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--;

                if (countdown > 0) {
                    turnLabel.setText("Next turn in: " + countdown);
                }
                else {
                    countdownTimer.stop();
                    playerOnesTurn = !playerOnesTurn;
                    turnLocked = false;
                    setBoardsEnabled(true);

                    if (playerOnesTurn) {
                        turnLabel.setText("Player 1's Turn");
                    }
                    else {
                        turnLabel.setText("Player 2's Turn");
                    }
                }
            }
        });

    countdownTimer.start();
}

private void setBoardsEnabled(boolean enabled) {
    for (Component c : board1.getComponents()) {
        c.setEnabled(enabled);
    }
    for (Component c : board2.getComponents()) {
        c.setEnabled(enabled);
    }
}

    public static void main(String[] args) {
        new Battleship();
    }
}