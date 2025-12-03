import java.util.ArrayList;

public class Board {

    public static final int WATER = 0;
    public static final int SHIP = 1;
    public static final int MISS = 2;
    public static final int HIT = 3;

    private final int[][] grid;
    private final ArrayList<Ship> ships = new ArrayList<>();

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

    public boolean hasShip(int row, int col) {
        return grid[row][col] == SHIP;
    }

    public boolean isWater(int row, int col) {
        return grid[row][col] == WATER;
    }




    public boolean canPlaceShip(int startRow, int startCol, int length, boolean horizontal){
        int size = grid.length;

        if(horizontal){
            if(startCol + length > length){
                return false;
            }
            for(int column = startCol; column < startCol + length; column++){
                if(grid[startRow][column] != WATER){
                    return false;
                }
            }
        }
        else {
            if(startRow + length > length){
                return false;
            }
            for(int row = startRow; row < startRow + length; row++){
                if(grid[row][startCol] != WATER){
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


    public void placeShipsRandomly(){
        Ship[] fleet = {
                new Carrier(),
                new BattleshipShip(),
                new Cruiser(),
                new Destroyer(),
        };

        int size = grid.length;

        for (Ship ship : fleet){
            boolean placed = false;
            while(!placed){
                boolean horizontal = Math.random() < 0.5;
                int row = (int) (Math.random() * size);
                int col = (int) (Math.random() * size);

                placed = placeShipOnBoard(ship, row, col, horizontal);
            }
        }
    }

}
