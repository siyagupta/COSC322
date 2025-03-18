
package ubc.cosc322;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

public class COSC322Test extends GamePlayer {

    private SearchTree search;
    private GameClient gameClient = null;
    private BaseGameGUI gamegui = null;
    private String userName = null;
    private String passwd = null;
    private boolean gameStarted = false;
    private GameRules ourBoard = null;
    int turnCount = 0;
    String ourPlayer = "";
    String enemyPlayer = "";

    public static void main(String[] args) {
        COSC322Test player = new COSC322Test("cosc322", "cosc322");

        if (player.getGameGUI() == null) {
            player.Go();
        } else {
            BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(() -> player.Go());
        }
    }

    public COSC322Test(String userName, String passwd) {
        this.userName = userName;
        this.passwd = passwd;
        this.gamegui = new BaseGameGUI(this);
    }

    @Override
    public void onLogin() {
        if (gameClient != null) {
            System.out.println(gameClient.getRoomList());
            gameClient.joinRoom("Kalamalka Lake");
            System.out.println("Congratulations! Login successful.");
            this.userName = gameClient.getUserName();
            if (gamegui != null) {
                gamegui.setRoomInformation(gameClient.getRoomList());
            }
        }
    }

    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
        if (messageType.equals(GameMessage.GAME_ACTION_START)) {
            String whitePlayer = (String) msgDetails.get("player-white");
            String blackPlayer = (String) msgDetails.get("player-black");

            if (whitePlayer.equals(this.userName)) {
                ourPlayer = "White Player: " + this.userName;
                enemyPlayer = "Black Player: " + blackPlayer;
                ourBoard = new GameRules(true);
                search = new SearchTree(new SearchTreeNode(ourBoard));

                try {
                    SearchTreeNode ourBestMove = search.makeMove();
                    if (ourBestMove != null) {
                        Queen ourMove = ourBestMove.getQueen();
                        Arrow ourArrow = ourBestMove.getArrowShot();

                        int[] qcurr = ourMove.getOldPosition();
                        int[] qnew = ourMove.getNewPosition();

                        // Apply coordinate translation so the board marks the queen correctly.
                        gameClient.sendMoveMessage(
                            toArrayList(new int[]{ translateRow(qcurr[0]), translateCol(qcurr[1]) }),
                            toArrayList(new int[]{ translateRow(qnew[0]), translateCol(qnew[1]) }),
                            toArrayList(new int[]{ translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition()) })
                        );
                    }
                } catch (ExecutionException ignored) {}
            }
        }
        return true;
    }

    private void handleOpponentMove(Map<String, Object> msgDetails) throws CloneNotSupportedException, ExecutionException {
        turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | Move: " + ourPlayer + " | " + enemyPlayer);

        // These positions come from the server; assume they are already in the correct coordinate system.
        ArrayList<Integer> qcurr = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
        ArrayList<Integer> qnew = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
        ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);

        Queen enemyQueen = new Queen(convertRow(qnew.get(0)), convertCol(qnew.get(1)), true);
        enemyQueen.previousRow = convertRow(qcurr.get(0));
        enemyQueen.previousCol = convertCol(qcurr.get(1));

        Arrow enemyArrow = new Arrow(convertRow(arrow.get(0)), convertCol(arrow.get(1)));
        search.makeMoveOnRoot(enemyQueen, enemyArrow);

        // For moves received from the server, we assume they are already translated.
        gameClient.sendMoveMessage(
                new ArrayList<>(List.of(qcurr.get(0), qcurr.get(1))),
                new ArrayList<>(List.of(qnew.get(0), qnew.get(1))),
                new ArrayList<>(List.of(arrow.get(0), arrow.get(1)))
        );

        ourBoard.canEnemyMove();
        ourBoard.updateLegalQueenMoves();
        ourBoard.printBoard();

        if (ourBoard.goalTest()) {
            System.out.println("\nTHE GAME IS NOW OVER\n");
            return;
        }

        turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | Move: " + ourPlayer + " | " + enemyPlayer);

        SearchTreeNode ourBestMove = search.makeMove();
        if (ourBestMove != null) {
            Queen ourMove = ourBestMove.getQueen();
            Arrow ourArrow = ourBestMove.getArrowShot();
            gameClient.sendMoveMessage(
                    toArrayList(ourMove.combinedMove(translateRow(ourMove.previousRow), translateCol(ourMove.previousCol))),
                    toArrayList(ourMove.combinedMove(translateRow(ourMove.row), translateCol(ourMove.col))),
                    toArrayList(ourArrow.combinedMove(translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition())))
            );
        }

        if (ourBoard.goalTest()) {
            System.out.println("\nTHE GAME IS NOW OVER\n");
        }
    }

    private int convertRow(int row) {
        // Convert from server's coordinate system to our internal system.
        return Math.abs(row - 10);
    }

    private int convertCol(int col) {
        return (col - 1);
    }

    // These translation methods convert our board's coordinates to the server's system.
    private int translateRow(int row) {
        return Math.abs(10 - row);
    }

    private int translateCol(int col) {
        return col + 1;
    }

    public void playerMove(int x, int y, int arow, int acol, int qfr, int qfc) {
        // In a human move the board is already in our coordinate system.
        int[] qf = new int[]{qfr, qfc};
        int[] qn = new int[]{x, y};
        int[] ar = new int[]{arow, acol};

        // For human moves, send the raw coordinates (or add translation if needed)
        gameClient.sendMoveMessage(
                toArrayList(qf),
                toArrayList(qn),
                toArrayList(ar)
        );
    }

    // Helper method: converts an int[] to an ArrayList<Integer>
    private ArrayList<Integer> toArrayList(int[] arr) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i : arr) {
            list.add(i);
        }
        return list;
    }

    @Override
    public GameClient getGameClient() {
        // Minimal implementation; code preserved.
        return gameClient;
    }

    @Override
    public BaseGameGUI getGameGUI() {
        // Minimal implementation; code preserved.
        return gamegui;
    }

    @Override
    public void connect() {
        // Minimal implementation; code preserved.
    }

    @Override
    public String userName() {
        // Minimal implementation; code preserved.
        return userName;
    }
}
