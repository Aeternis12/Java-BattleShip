import java.util.ArrayList;

public class Board {

    public static final int WATER = 0;
    public static final int SHIP = 1;
    public static final int MISS = 2;
    public static final int HIT = 3;

    private final int[][] grid;
    private final ArrayList<Ship> ships = new ArrayList<>();


    // -------- GRID STATE TRACKING ------- \\
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
    public boolean allShipsSunk() {
        for(Ship s : ships) {
            if(!s.isSunk())
                return false;
        }
        return true;
    }

    public boolean isHit(int row, int col) {
        return grid[row][col] == MISS || grid[row][col] == HIT;
    }
    public boolean isShip(int row, int col) {
        return grid[row][col] == SHIP;
    }
    public boolean isWater(int row, int col) {
        return grid[row][col] == WATER;
    }
    public Ship getShipAt(int row, int col) {
        for(Ship ship : ships) {
            if(ship.occupiesGridLocation(row, col)) {
                return ship;
            }
        }
        return null;
    }



    // -------- SHIP PLACEMENT LOGIC -------- \\
    public boolean canPlaceShip(int startRow, int startCol, int length, boolean horizontal) {
        int size = grid.length;

        if(horizontal){
            if(startCol + length > size)
                return false;
        }
        else{
            if(startRow + length > size)
                return false;
        }

        java.util.function.BiPredicate<Integer, Integer> isWaterSafe = (r, c) -> {
            if (r < 0 || c < 0 || r >= size || c >= size)
                return true;  // out of bounds = safe
            return grid[r][c] == WATER;
        };

        //Check ship squares and the surrounding squares
        if (horizontal) {
            for (int c = startCol - 1; c <= startCol + length; c++) {
                for (int r = startRow - 1; r <= startRow + 1; r++) {
                    if (!isWaterSafe.test(r, c))
                        return false;
                }
            }
        }
        else {
            for (int r = startRow - 1; r <= startRow + length; r++) {
                for (int c = startCol - 1; c <= startCol + 1; c++) {
                    if (!isWaterSafe.test(r, c))
                        return false;
                }
            }
        }

        return true;
    }

    public boolean placeShipOnBoard(Ship ship, int startRow, int startCol, boolean horizontal){
        int shipLength = ship.getLength();

        if(!canPlaceShip(startRow, startCol, shipLength, horizontal)){
            return false;
        }

        if(horizontal){
            for (int column = startCol; column < startCol + shipLength; column++){
                grid[startRow][column] = SHIP;
            }
        }
        else{
            for (int row = startRow; row < startRow + shipLength; row++){
                grid[row][startCol] = SHIP;
            }
        }

        ship.placeShip(startRow, startCol, horizontal);

        ships.add(ship);

        return true;
    }

    public void placeShipsRandomly(Ship[] fleet){
        int size = grid.length;

        for (Ship ship : fleet) {
            int length = ship.getLength();
            boolean placed = false;

            while (!placed) {
                boolean horizontal = Math.random() < 0.5;
                int row = (int) (Math.random() * size);
                int col = (int) (Math.random() * size);

                if (!canPlaceShip(row, col, length, horizontal)) {
                    continue;
                }
                placed = placeShipOnBoard(ship, row, col, horizontal);
            }
        }
    }

    // -------- SALVO MODE ------------ \\
    public int getUnsunkCount() {
        int count = 0;
        for(Ship s : ships) {
            if(!s.isSunk())
                count++;
        }
        return count;
    }

}
