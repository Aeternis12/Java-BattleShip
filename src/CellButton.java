import javax.swing.*;

public class CellButton extends JButton {
    public final int row;
    public final int col;
    public final boolean isLeftBoard;

    public CellButton(int row, int col, boolean isLeftBoard) {
        this.row = row;
        this.col = col;
        this.isLeftBoard = isLeftBoard;
    }
}
