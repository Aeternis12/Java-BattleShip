public class Board {

    public static final int WATER = 0;
    public static final int SHIP = 1;
    public static final int MISS = 2;
    public static final int HIT = 3;

    private int[][] cells;

    public Board(int size){
        cells = new int[size][size];
    }

    public int getCell(int row, int col) {
        return cells[row][col];
    }

    public void setCell(int row, int col, int value) {
        cells[row][col] = value;
    }
}
