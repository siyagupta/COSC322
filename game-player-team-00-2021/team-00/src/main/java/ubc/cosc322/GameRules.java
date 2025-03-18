package ubc.cosc322;

import java.util.ArrayList;

public class GameRules {
    private boolean enemyMove;
    protected Tile[][] board = new Tile[10][10];
    protected Queen[] enemy;
    protected Queen[] friend;
    protected ArrayList<Arrow> arrows = new ArrayList<>();
    private ArrayList<Queen> legalQueenMoves = new ArrayList<>();

    /**
     * Constructor to initialize the board and set up queens based on player turn
     * @param start - true if our player moves first (white), false if opponent moves first (black)
     */
    public GameRules(boolean start) {
        initializeBoard(start);
        updateLegalQueenMoves();
        enemyMove = canEnemyMove();
    }

    /**
     * Initializes the board and sets queen positions based on whether we are starting first.
     */
    private void initializeBoard(boolean start) {
        if (start) {
            friend = new Queen[]{
                new Queen(6, 0, false), new Queen(6, 9, false),
                new Queen(9, 3, false), new Queen(9, 6, false)
            };
            enemy = new Queen[]{
                new Queen(0, 3, true), new Queen(0, 6, true),
                new Queen(3, 0, true), new Queen(3, 9, true)
            };
        } else {
            friend = new Queen[]{
                new Queen(0, 3, false), new Queen(0, 6, false),
                new Queen(3, 0, false), new Queen(3, 9, false)
            };
            enemy = new Queen[]{
                new Queen(6, 0, true), new Queen(6, 9, true),
                new Queen(9, 3, true), new Queen(9, 6, true)
            };
        }
        updateAfterMove();
    }

    /**
     * Deep copy of the current game state
     * @return a new GameRules object representing the same board state
     */
    protected GameRules deepCopy() {
        Queen[] newFriend = new Queen[4];
        Queen[] newEnemy = new Queen[4];
        ArrayList<Arrow> newArrows = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            newFriend[i] = friend[i].clone();
            newEnemy[i] = enemy[i].clone();
        }
        for (Arrow a : arrows) {
            newArrows.add(new Arrow(a.row, a.col));
        }

        GameRules newRules = new GameRules(newEnemy, newFriend, newArrows);
        return newRules;
    }

    /**
     * Constructor for deepCopy use
     */
    protected GameRules(Queen[] enemy, Queen[] friend, ArrayList<Arrow> arrow) {
        this.enemy = enemy;
        this.friend = friend;
        this.arrows = arrow;
        updateAfterMove();
    }

    /**
     * @return - Array of opponent's queens
     */
    protected Queen[] getEnemy() {
        return this.enemy;
    }

    /**
     * @return - Array of player's queens
     */
    protected Queen[] getFriend() {
        return this.friend;
    }

    /**
     * @return - whether the opponent has legal moves left
     */
    public boolean canEnemyMove() {
        for (Queen q : enemy) {
            if (!getLegalMoves(q).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the board state after a move is made
     */
    protected void updateAfterMove() {
        clearBoard();
        for (Queen q : friend) board[q.row][q.col] = q;
        for (Queen q : enemy) board[q.row][q.col] = q;
        for (Arrow a : arrows) board[a.row][a.col] = a;
    }

    /**
     * Clears the board for fresh state update
     */
    private void clearBoard() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                board[i][j] = null;
            }
        }
    }

    /**
     * Adds an arrow to the board
     * @param newArrow - The new arrow position
     */
    protected void addArrow(Arrow newArrow) {
        arrows.add(newArrow);
        updateAfterMove();
    }

    /**
     * @return - Whether the game is over
     */
    protected boolean goalTest() {
        return !enemyMove || legalQueenMoves.isEmpty();
    }

    /**
     * Updates the possible moves for all friendly queens
     */
    public void updateLegalQueenMoves() {
        legalQueenMoves.clear();
        for (Queen q : friend) {
            legalQueenMoves.addAll(getLegalMoves(q));
        }
    }

    /**
     * Prints the current board state
     */
    public void printBoard() {
        StringBuilder boardLayout = new StringBuilder();
        String line = "\n+---+---+---+---+---+---+---+---+---+---+";

        for (int i = 0; i < 10; i++) {
            boardLayout.append(line).append("\n");
            for (int j = 0; j < 10; j++) {
                boardLayout.append("| ");
                if (board[i][j] == null) boardLayout.append("  ");
                else if (board[i][j] instanceof Queen) {
                    if (board[i][j] == enemy[0] || board[i][j] == enemy[1] ||
                        board[i][j] == enemy[2] || board[i][j] == enemy[3]) {
                        boardLayout.append("B ");
                    } else boardLayout.append("W ");
                } else boardLayout.append("X ");
            }
            boardLayout.append("|");
        }
        boardLayout.append(line);
        System.out.println(boardLayout);
    }

    /**
     * Finds legal moves for a given queen
     * @param queen - The queen whose moves we are checking
     * @return - List of legal moves
     */
    protected ArrayList<Queen> getLegalMoves(Queen queen) {
        ArrayList<Queen> legalMoves = new ArrayList<>();
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},  // Vertical & Horizontal
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1} // Diagonal
        };

        for (int[] dir : directions) {
            int newRow = queen.row;
            int newCol = queen.col;
            while (true) {
                newRow += dir[0];
                newCol += dir[1];
                if (newRow < 0 || newRow > 9 || newCol < 0 || newCol > 9 || board[newRow][newCol] != null) {
                    break;
                }
                legalMoves.add(new Queen(newRow, newCol));
            }
        }
        return legalMoves;
    }
}
