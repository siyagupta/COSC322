package ubc.cosc322;

import java.util.ArrayList;

public class Arrow extends Tile implements Cloneable {
    protected Arrow clone() {
        Arrow aNew = new Arrow(row, col);
        return aNew;
    }
    
    public Arrow(int i, int j) {
        super(i, j);
    }

    // Static method to get an Arrow by index
    public static Arrow get(int index) {
        if (index >= 0) {
            return Arrow.get(index);
        }
        return null;  // Or throw an exception
    }

    public int getColPosition() {
        return super.col;
    }

    public int getRowPosition() {
        return super.row;
    }

    public int[] combinedMove(int row, int col) {
        int[] move = new int[2];
        move[0] = row;
        move[1] = col;
        return move;
    }
    
    /**
     * Returns the arrow's position as an ArrayList<Integer>.
     */
    public ArrayList<Integer> getPosition() {
        ArrayList<Integer> pos = new ArrayList<>();
        pos.add(getRowPosition());
        pos.add(getColPosition());
        return pos;
    }
}
