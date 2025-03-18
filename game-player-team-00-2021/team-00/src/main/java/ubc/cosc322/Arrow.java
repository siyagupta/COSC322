package ubc.cosc322;

public class Arrow extends Tile implements Cloneable {

    @Override
    protected Arrow clone() {
        try {
            return (Arrow) super.clone();
        } catch (CloneNotSupportedException e) {
            return new Arrow(this.row, this.col); // Fallback in case of an issue
        }
    }

    public Arrow(int i, int j) {
        super(i, j);
    }

    public int getColPosition() {
        return this.col;
    }

    public int getRowPosition() {
        return this.row;
    }

    public int[] combinedMove(int row, int col) {
        return new int[]{row, col};
    }
}
