
package ubc.cosc322;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;

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
            if (((String) msgDetails.get("player-white")).equals(this.userName())) {
                System.out.println("Game State: " + msgDetails.get("player-white"));
                ourPlayer = "White Player: " + this.userName();
                enemyPlayer = "Black Player: " + msgDetails.get("player-black");
                turnCount++;
                gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);
                ourBoard = new GameRules(true);
                System.out.println("Initial Board");
                ourBoard.printBoard();
                ourBoard.canEnemyMove();
                ourBoard.updateLegalQueenMoves();
                search = new SearchTree(new SearchTreeNode(ourBoard));
                SearchTreeNode ourBestMove = null;
                try {
                    ourBestMove = search.makeMove();
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                }
                Queen ourMove = ourBestMove.getQueen();
                Arrow ourArrow = ourBestMove.getArrowShot();
                ourBoard.canEnemyMove();
                ourBoard.updateLegalQueenMoves();
                System.out.println("\nOur Move: [" + translateRow(ourMove.row) + ", " + translateCol(ourMove.col) + "]");
                System.out.println("Our Arrow Shot: [" + translateRow(ourArrow.row) + ", " + translateCol(ourArrow.col) + "]\n");

                ourBoard.printBoard();

            } else {
                ourPlayer = "Black Player: " + this.userName();
                enemyPlayer = "White Player: " + msgDetails.get("player-white");
                ourBoard = new GameRules(false);
                search = new SearchTree(new SearchTreeNode(ourBoard));

            }
        } else if (messageType.equals(GameMessage.GAME_ACTION_MOVE)) {
            // Handle opponent move if necessary.
        }

        ArrayList<Integer> GameS = (ArrayList<Integer>) msgDetails.get("game-state");
        gamegui.setGameState(GameS);

        return true;
    }

    private void handleOpponentMove(Map<String, Object> msgDetails) throws CloneNotSupportedException, ExecutionException {
        turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);
        ArrayList<Integer> qcurr = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
        ArrayList<Integer> qnew = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
        ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
        
        Queen enemyQueen = new Queen(convertRow(qnew.get(0)), convertCol(qnew.get(1)), true);
        enemyQueen.previousRow = convertRow(qcurr.get(0));
        enemyQueen.previousCol = convertCol(qcurr.get(1));
        Arrow enemyArrow = new Arrow(convertRow(arrow.get(0)), convertCol(arrow.get(1)));
        search.makeMoveOnRoot(enemyQueen, enemyArrow);
        
        markArrow(qnew, arrow, qcurr, true);
        ourBoard.canEnemyMove();
        ourBoard.updateLegalQueenMoves();
        ourBoard.printBoard();
        
        if(ourBoard.goalTest()) {
            System.out.println("\n THE GAME IS NOW OVER \n");
            return;
        }
        
        turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);
        SearchTreeNode ourBestMove = search.makeMove();
        executeMove(ourBestMove);
    }


    private int convertRow(int row) {
        return Math.abs(row - 10); // formula to convert server's row coordinate system to our Board's coordinate system
    }

    private int convertCol(int col) {
        return (col - 1); // formula to convert server's column coordinate system to our Board's coordinate system
    }

    private int translateCol(int col) {
        return (col + 1); // formula to translate our Board's column coordinate system to the server's coordinate system
    }

    private int translateRow(int row) {
        return Math.abs(10 - row); // formula to convert our Board's row coordinate system to the server's coordinate system
    }

    @Override
    public String userName() {
        return userName;
    }

    @Override
    public GameClient getGameClient() {
        return this.gameClient;
    }

    @Override
    public BaseGameGUI getGameGUI() {
        return this.gamegui;
    }

    @Override
    public void connect() {
        gameClient = new GameClient(userName, passwd, this);
    }
}
