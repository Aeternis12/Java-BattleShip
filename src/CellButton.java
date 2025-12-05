import javax.swing.*;
import java.awt.*;

public class CellButton extends JButton {

    public static final int none = 0;
    public static final int miss = 1;
    public static final int hit = 2;
    public static final int ship = 3;
    public static final int hidden = 4;

    public static final int hoverGood = 5;
    public static final int hoverBad = 6;

    public final int row;
    public final int col;
    public final boolean isLeftBoard;
    private int state = none;

    public CellButton(int row, int col, boolean isLeftBoard) {
        this.row = row;
        this.col = col;
        this.isLeftBoard = isLeftBoard;

        setFocusPainted(false);
        setContentAreaFilled(true);
        setOpaque(true);
        setBackground(new Color(173, 216, 230));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    public void setState(int state) {
        this.state = state;
        repaint();
    }
    public int getState() {
        return state;
    }

    // -------- VIEW LOGIC ------- \\
    @Override
    protected void paintComponent(Graphics g) {
        //Function paints the CellButton, determining its color by the state of the Cell itself, which is based on the
        //board logic
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(state == none) 
            return;
        int d = Math.min(getWidth(), getHeight()) - 12;
        if(state == miss){
            g2.setColor(Color.WHITE);
            g2.fillOval((getWidth()-d)/2, (getHeight()-d)/2, d, d);
        }
        else if(state == hit) {
            g2.setColor(Color.RED);
            g2.fillOval((getWidth()-d)/2, (getHeight()-d)/2, d, d);
        }
        else if(state == ship) {
            g2.setColor(new Color(100, 100, 100));
            g2.fillRect(4, 4, getWidth() - 8, getHeight() - 8);
        }
        else if(state == hidden) {
            g2.setColor(new Color(126, 204, 241));
            g2.fillRect(4, 4, getWidth() - 8, getHeight() - 8);
        }
        else if(state == hoverGood) {
            g2.setColor(new Color(0,200,0));
            g2.fillRect(4, 4, getWidth() - 8, getHeight() - 8);
        }
        else if(state == hoverBad) {
            g2.setColor(new Color(200,0,0));
            g2.fillRect(4, 4, getWidth() - 8, getHeight() - 8);
        }
    }
}
