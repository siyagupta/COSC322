package ubc.cosc322;

import java.util.ArrayList;
import java.util.HashMap;
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
        COSC322Test player = new COSC322Test("cosc322c", "cosc322c");

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
        this.connect(); // <-- Missing connect call added here
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
        System.out.printf("Received game message:\n\tType: %s\n\tDetails: %s\n", messageType, msgDetails);
        switch (messageType) {
            case GameMessage.GAME_STATE_BOARD:
                ArrayList<Integer> gameState = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);
                gamegui.setGameState(gameState);
                break;

            case GameMessage.GAME_ACTION_MOVE:
                ArrayList<Integer> queenCurrent = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
                ArrayList<Integer> queenNew = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
                ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
                gamegui.updateGameState(queenCurrent, queenNew, arrow);
                try {
                    handleOpponentMove(msgDetails);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case GameMessage.GAME_ACTION_START:
                System.out.println("Game is ready to start!");
                String whitePlayer = (String) msgDetails.get(AmazonsGameMessage.PLAYER_WHITE);
                String blackPlayer = (String) msgDetails.get(AmazonsGameMessage.PLAYER_BLACK);

                ourPlayer = userName();  // our username
                enemyPlayer = whitePlayer.equals(ourPlayer) ? blackPlayer : whitePlayer;

                System.out.println("White: " + whitePlayer + ", Black: " + blackPlayer);

                ourBoard = new GameRules(gameStarted);
                SearchTreeNode root = new SearchTreeNode(ourBoard);
                search = new SearchTree(root);

                boolean weAreWhite = whitePlayer.equals(ourPlayer);

                if (weAreWhite) {
                    try {
                        System.out.println("We are WHITE. Making first move...");
                        turnCount++;
                        gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);
                        SearchTreeNode ourBestMove = search.makeMove();
                        executeMove(ourBestMove);
                    } catch (ExecutionException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("We are BLACK. Waiting for opponent's move...");
                }
                break;
        }
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

        if (ourBoard.goalTest()) {
            System.out.println("\n THE GAME IS NOW OVER \n");
            return;
        }

        turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);
        SearchTreeNode ourBestMove = search.makeMove();
        executeMove(ourBestMove);
    }

    private int convertRow(int row) {
        return row - 1;
        //return Math.abs(row - 10);
    }

    private int convertCol(int col) {

        return col - 1;
    }

    private int translateCol(int col) {
        return col + 1;
    }

    private int translateRow(int row) {
        return Math.abs(10 - row);
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

    private void markArrow(ArrayList<Integer> qnew, ArrayList<Integer> arrow, ArrayList<Integer> qcurr, boolean isOpponent) {
        int arrowRow = convertRow(arrow.get(0));
        int arrowCol = convertCol(arrow.get(1));

        // Basic version just prints where the arrow is
        System.out.printf("Marking arrow on board at (row=%d, col=%d)\n", arrowRow, arrowCol);

        // // Optional: if your GameRules or board class supports marking arrows
        // if (ourBoard != null) {
        //     ourBoard.placeArrow(arrowRow, arrowCol); // <-- You must implement this in GameRules if not present
        // }
    }

    private void executeMove(SearchTreeNode ourBestMove) {
        if (ourBestMove == null) {
            System.out.println("Invalid move!");
            return;
        }

        

        try {
            System.out.println("Entered");
            GameClient gameClient = getGameClient();
            Map<String, Object> moveMessage = new HashMap<>();
            // moveMessage.put(AmazonsGameMessage.QUEEN_POS_CURR, ourBestMove.getQueenPosCurr());
            // moveMessage.put(AmazonsGameMessage.QUEEN_POS_NEXT, ourBestMove.getQueenPosNext());
            // moveMessage.put(AmazonsGameMessage.ARROW_POS, ourBestMove.getArrowPos());
            ArrayList<Integer> qCurr = ourBestMove.getQueenPosCurr();
ArrayList<Integer> qNext = ourBestMove.getQueenPosNext();
ArrayList<Integer> arrow = ourBestMove.getArrowPos();

ArrayList<Integer> qCurrTranslated = new ArrayList<>();
ArrayList<Integer> qNextTranslated = new ArrayList<>();
ArrayList<Integer> arrowTranslated = new ArrayList<>();

qCurrTranslated.add(translateRow(qCurr.get(0)));
qCurrTranslated.add(translateCol(qCurr.get(1)));

qNextTranslated.add(translateRow(qNext.get(0)));
qNextTranslated.add(translateCol(qNext.get(1)));

arrowTranslated.add(translateRow(arrow.get(0)));
arrowTranslated.add(translateCol(arrow.get(1)));

moveMessage.put(AmazonsGameMessage.QUEEN_POS_CURR, qCurrTranslated);
moveMessage.put(AmazonsGameMessage.QUEEN_POS_NEXT, qNextTranslated);
moveMessage.put(AmazonsGameMessage.ARROW_POS, arrowTranslated);

            System.out.println(ourBestMove.getQueenPosCurr().getClass());

            System.out.println("Ok");
            gameClient.sendMoveMessage(moveMessage);
            System.out.println("Moved");
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
    }
}
