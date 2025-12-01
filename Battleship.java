import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Battleship extends JFrame {
    private static final int SIZE = 10;
    private static final int CELL_SIZE = 40;

    public Battleship() {
        setTitle("Battleship");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2, 40, 0));

        JPanel board1 = createBoard(); // Player 1 clicks here
        JPanel board2 = createBoard(); // Player 2 clicks here (turn based logic still needs to be setup)

        mainPanel.add(board1);
        mainPanel.add(board2);

        add(mainPanel);

        pack(); // Sizes window based on button sizes
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createBoard() {
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
                    if(!cell.getBackground().equals(Color.WHITE)) { //will need to be changed when we add ships
                        cell.setBackground(Color.WHITE);
                    }
                }
            });

            board.add(cell);
        }

        return board;
    }

    public static void main(String[] args) {
        new Battleship();
    }
}