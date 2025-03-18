
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

                gameClient.sendMoveMessage(
                new ArrayList<>(List.of(qcurr[0], qcurr[1])), 
                new ArrayList<>(List.of(qnew[0], qnew[1])), 
                new ArrayList<>(List.of(Arrow.get(0), Arrow.get(1)))
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

        ArrayList<Integer> qcurr = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
        ArrayList<Integer> qnew = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
        ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);

        Queen enemyQueen = new Queen(convertRow(qnew.get(0)), convertCol(qnew.get(1)), true);
        enemyQueen.previousRow = convertRow(qcurr.get(0));
        enemyQueen.previousCol = convertCol(qcurr.get(1));

        Arrow enemyArrow = new Arrow(convertRow(arrow.get(0)), convertCol(arrow.get(1)));
        search.makeMoveOnRoot(enemyQueen, enemyArrow);

        gameClient.sendMoveMessage(
                new int[]{qcurr.get(0), qcurr.get(1)},
                new int[]{qnew.get(0), qnew.get(1)},
                new int[]{arrow.get(0), arrow.get(1)}
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
                    new int[]{ourMove.previousRow, ourMove.previousCol},
                    new int[]{ourMove.row, ourMove.col},
                    new int[]{ourArrow.getRowPosition(), ourArrow.getColPosition()}
            );
        }

        if (ourBoard.goalTest()) {
            System.out.println("\nTHE GAME IS NOW OVER\n");
        }
    }

    private int convertRow(int row) {
        return Math.abs(row - 10);
    }

    private int convertCol(int col) {
        return (col - 1);
    }

    public void playerMove(int x, int y, int arow, int acol, int qfr, int qfc) {
        gameClient.sendMoveMessage(
                new int[]{qfr, qfc}, new int[]{x, y}, new int[]{arow, acol}
        );
    }

    @Override
    public GameClient getGameClient() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BaseGameGUI getGameGUI() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void connect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String userName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
