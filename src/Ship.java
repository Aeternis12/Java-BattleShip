public abstract class Ship {

    protected final String name;
    protected final int length;
    protected int startRow = -1;
    protected int startCol = -1;
    protected boolean horizontal = true;
    protected int hitCount;

    public Ship(String name, int length) {
        this.name = name;
        this.length = length;
    }

    protected String  getName() {
        return name;
    }
    protected int getLength() {
        return length;
    }
    protected int getStartRow() {
        return startRow;
    }
    protected int getStartCol() {
        return startCol;
    }
    protected boolean isHorizontal() {
        return horizontal;
    }


    // ------ STATE TRACKING ------ \\
    public void placeShip(int row, int col, boolean horizontal) {
        //Places ship at a grid location
        this.startRow = row;
        this.startCol = col;
        this.horizontal = horizontal;
    }
    public boolean occupiesGridLocation(int row, int col) {
        //Function checks to see if a ship occupies a specific grid location
        if (startRow < 0 || startCol < 0){
            return false;
        }
        if (horizontal){
            return row == startRow && col >= startCol && col < startCol + length;
        }
        else {
            return col == startCol && row >= startRow && row < startRow + length;
        }
    }
    public boolean shipHasBeenHit (int row, int col) {
        //Function sees if ship is at a location, then increments the hitcount is it is)
        if (!occupiesGridLocation(row, col)) {
            return false;
        }

        if(hitCount < length){
            hitCount++;
        }
        return true;
    }
    public boolean isSunk(){
        return hitCount >= length;
    }
}
