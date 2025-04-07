package ubc.cosc322;

import java.util.ArrayList;

public class Queen extends Tile implements Cloneable {
    public int previousRow;
    public int previousCol;
    protected boolean isOpponent;

    /**
     * Overloaded constructor to allow instantiation with two arguments.
     * Defaults isOpponent to false.
     */
    public Queen(int row, int col) {
        super(row, col);
        this.isOpponent = false;  // default value
        this.previousRow = row;
        this.previousCol = col;
    }

    /**
     * Constructor that accepts row, col, and isOpponent.
     */
    public Queen(int row, int col, boolean isOpponent) {
        super(row, col);
        this.isOpponent = isOpponent;
        this.previousRow = row;
        this.previousCol = col;
    }

    /**
     * Moves the queen to a new position.
     * Updates the previous position accordingly.
     */
    public void moveQueen(int newRow, int newCol) {
        // Save current position as previous
        this.previousRow = this.row;
        this.previousCol = this.col;
        // Update to new position
        this.row = newRow;
        this.col = newCol;
    }

    /**
     * clone: creates a Queen object at its new location
     * @return: Queen object with its new location
     */
    protected Queen clone() {
        Queen qNew = new Queen(row, col, isOpponent);
        return qNew;
    }
    
    /**
     * Returns the queen's current (destination) position as an ArrayList<Integer>.
     */
    public ArrayList<Integer> getCurrentPosition() {
        ArrayList<Integer> pos = new ArrayList<>();
        pos.add(this.row);  // current row
        pos.add(this.col);  // current column
        return pos;
    }
    
    /**
     * Returns the queen's previous (starting) position as an ArrayList<Integer>.
     */
    public ArrayList<Integer> getPreviousPosition() {
        ArrayList<Integer> pos = new ArrayList<>();
        pos.add(this.previousRow);
        pos.add(this.previousCol);
        return pos;
    }
}
