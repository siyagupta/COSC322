package ubc.cosc322;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;

/**
 * A GamePlayer implementation for the Game of Amazons
 */
public class COSC322Test extends GamePlayer {

    private SearchTree search;
    private GameClient gameClient = null;
    private BaseGameGUI gamegui = null;
    private GameRules ourBoard = null;
    private String userName = null;
    private String passwd = null;
    private int turnCount = 0;
    private String ourPlayer = "";
    private String enemyPlayer = "";

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
        System.out.println("Login successful. Joining a room...");
        gameClient.joinRoom("Kalamalka Lake");
        userName = gameClient.getUserName();
        
        if (gamegui != null) {
            gamegui.setRoomInformation(gameClient.getRoomList());
        }
    }

    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
        if (messageType.equals(GameMessage.GAME_ACTION_START)) {
            handleGameStart(msgDetails);
        } else if (messageType.equals(GameMessage.GAME_ACTION_MOVE)) {
            try {
                handleOpponentMove(msgDetails);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        if (msgDetails.containsKey("game-state")) {
            gamegui.setGameState((ArrayList<Integer>) msgDetails.get("game-state"));
        } else {
            System.out.println("Warning: Game state missing from message.");
        }
        return true;
    }

    private void handleGameStart(Map<String, Object> msgDetails) {
        String whitePlayer = (String) msgDetails.get("player-white");
        String blackPlayer = (String) msgDetails.get("player-black");
        
        ourPlayer = whitePlayer.equals(this.userName()) ? "White Player: " + this.userName() : "Black Player: " + this.userName();
        enemyPlayer = whitePlayer.equals(this.userName()) ? "Black Player: " + blackPlayer : "White Player: " + whitePlayer;

        ourBoard = new GameRules(whitePlayer.equals(this.userName()));
        search = new SearchTree(new SearchTreeNode(ourBoard));
        gamegui.setTitle(ourPlayer + " vs " + enemyPlayer);
        
        ourBoard.printBoard();

        if (ourPlayer.contains("White")) {
            makeMove();
        }
    }

    private void handleOpponentMove(Map<String, Object> msgDetails) throws CloneNotSupportedException {
        if (!msgDetails.containsKey(GameMessage.QUEEN_POS_CURR) ||
            !msgDetails.containsKey(GameMessage.QUEEN_POS_NEXT) ||
            !msgDetails.containsKey(GameMessage.ARROW_POS)) {
            System.out.println("Error: Missing move details from opponent.");
            return;
        }

        turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | " + ourPlayer + " vs " + enemyPlayer);

        ArrayList<Integer> qcurr = (ArrayList<Integer>) msgDetails.get(GameMessage.QUEEN_POS_CURR);
        ArrayList<Integer> qnew = (ArrayList<Integer>) msgDetails.get(GameMessage.QUEEN_POS_NEXT);
        ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(GameMessage.ARROW_POS);

        Queen enemyQueen = new Queen(convertRow(qnew.get(0)), convertCol(qnew.get(1)), true);
        enemyQueen.previousRow = convertRow(qcurr.get(0));
        enemyQueen.previousCol = convertCol(qcurr.get(1));
        Arrow enemyArrow = new Arrow(convertRow(arrow.get(0)), convertCol(arrow.get(1)));

        search.makeMoveOnRoot(enemyQueen, enemyArrow);
        ourBoard.printBoard();

        if (!ourBoard.goalTest()) {
            makeMove();
        } else {
            System.out.println("Game Over!");
        }
    }

    private void makeMove() {
        turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | " + ourPlayer + " vs " + enemyPlayer);

        SearchTreeNode ourBestMove = search.makeMove();
        if (ourBestMove == null) {
            System.out.println("No valid moves left. Game over.");
            return;
        }

        Queen ourMove = ourBestMove.getQueen();
        Arrow ourArrow = ourBestMove.getArrowShot();

        System.out.println("Move from: " + translateRow(ourMove.previousRow) + "," + translateCol(ourMove.previousCol));
        System.out.println("Move to: " + translateRow(ourMove.row) + "," + translateCol(ourMove.col));
        System.out.println("Arrow at: " + translateRow(ourArrow.getRowPosition()) + "," + translateCol(ourArrow.getColPosition()));

        gameClient.markPosition(translateRow(ourMove.row), translateCol(ourMove.col), 
                translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition()),
                translateRow(ourMove.previousRow), translateCol(ourMove.previousCol), false);

        gameClient.sendMoveMessage(
            List.of(translateRow(ourMove.previousRow), translateCol(ourMove.previousCol)),
            List.of(translateRow(ourMove.row), translateCol(ourMove.col)),
            List.of(translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition()))
        );

        ourBoard.printBoard();
    }

    private int convertRow(int row) {
        return Math.abs(row - 10);
    }

    private int convertCol(int col) {
        return col - 1;
    }

    private int translateRow(int row) {
        return Math.abs(10 - row);
    }

    private int translateCol(int col) {
        return col + 1;
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
