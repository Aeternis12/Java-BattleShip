public class Board {

    public static final int WATER = 0;
    public static final int SHIP = 1;
    public static final int MISS = 2;
    public static final int HIT = 3;

    private final int[][] grid;

    public Board(int size){
        this.grid = new int[size][size];
    }

    public int getCell(int row, int col) {
        return grid[row][col];
    }

    public void setCell(int row, int col, int value) {
        grid[row][col] = value;
    }


    public void markMiss(int row, int col) {
        grid[row][col] = MISS;
    }

    public void markHit(int row, int col) {
        grid[row][col] = HIT;
    }

    public boolean hasBeenShot(int row, int col) {
        return grid[row][col] == MISS || grid[row][col] == HIT;
    }
}
