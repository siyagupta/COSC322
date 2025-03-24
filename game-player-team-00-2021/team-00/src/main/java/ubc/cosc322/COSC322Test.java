package ubc.cosc322;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import sfs2x.client.entities.Room;
import ygraph.ai.smartfox.games.Amazon.GameBoard;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsBoard;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

public class COSC322Test extends GamePlayer {

    private SearchTree search;
    private GameClient gameClient = null;
    private BaseGameGUI gamegui = null;
    private GameBoard gameBoard = null;
    private String userName = null;
    private String passwd = null;
    protected localBoard amazonsBoard = new localBoard();
    protected ActionFactory actionFactory = new ActionFactory();
    private boolean gameStarted = false;
    private GameRules ourBoard = null;
    int turnCount = 0;
    String ourPlayer = "";
    String enemyPlayer = "";

    
    public static void main(String[] args) {
    	String uniqueUserName = "player_" + System.currentTimeMillis();
        COSC322Test player = new COSC322Test(uniqueUserName, "cosc322");
        MonteCarloPlayer playerMC = new MonteCarloPlayer();
        playerMC.connect();
        if (playerMC.getGameGUI() == null) {
            playerMC.Go();
        } else {
            BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(() -> playerMC.Go());
            
        }
    } 
   
    
    protected ArrayList<Actions> getAvailableActions() {
        return actionFactory.getActions(amazonsBoard);
      }
    public COSC322Test(String userName, String passwd) {
        this.userName =  "player_" + System.currentTimeMillis();;
        this.passwd = passwd;
        this.gamegui = new BaseGameGUI(this);
      /*  ygraph.ai.smartfox.games.Amazon amazonInstance = new ygraph.ai.smartfox.games.Amazon(userName, passwd);
        this.gameBoard = amazonInstance.new GameBoard(amazonInstance);
       this.amazonsBoard = new AmazonsBoard(); */
    }
    protected void sendMove(List<Integer> queenCurrent, List<Integer> queenTarget, List<Integer> arrowTarget) {
        amazonsBoard.updateState(queenCurrent, queenTarget, arrowTarget);
        amazonsBoard.printState();
        gamegui.updateGameState(new ArrayList<>(queenCurrent), new ArrayList<>(queenTarget), new ArrayList<>(arrowTarget));
        gameClient.sendMoveMessage(new ArrayList<>(queenCurrent), new ArrayList<>(queenTarget), new ArrayList<>(arrowTarget));
      }

    /** Called when the player receives a move message from the server. */
   
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
private void updateGameGUI(Map<String, Object> msgDetails) {
    if (gamegui != null && amazonsBoard != null) {
        gamegui.setGameState( amazonsBoard.getState());
    }
}
    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
    	switch(messageType) {
		case GameMessage.GAME_STATE_BOARD: handleBoardMessage(msgDetails); break;
		case GameMessage.GAME_ACTION_START: handleGameStart(messageType, msgDetails); break;
		case GameMessage.GAME_ACTION_MOVE: try {
				handleOpponentMove(msgDetails);
			} catch (CloneNotSupportedException e) {
				
				e.printStackTrace();
			} catch (ExecutionException e) {
			
				e.printStackTrace();
			} break;
		}
		return true; 
	/*
    	if (messageType.equals(GameMessage.GAME_ACTION_START)) {
            if (((String) msgDetails.get("player-white")).equals(this.userName())) {
                System.out.println("Game State: " + msgDetails.get("player-white"));
                ourPlayer = "White Player: " + this.userName;
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
                gameBoard.markPosition(translateRow(ourMove.row), translateCol(ourMove.col), translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition()),
						translateRow(ourMove.previousRow), translateCol(ourMove.previousCol), false);
              //  gameClient.sendMoveMessage(ourMove.combinedMove(translateRow(ourMove.previousRow), translateCol(ourMove.previousCol)),
                   //     ourMove.combinedMove(translateRow(ourMove.row), translateCol(ourMove.col)),
                //        ourArrow.combinedMove(translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition())));
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

        return true; */
    }
    private void handleBoardMessage(Map<String, Object> msgDetails) {
		if (gamegui != null) {
			gamegui.setGameState(amazonsBoard.getState());
		}
		System.out.println(msgDetails.get(AmazonsGameMessage.GAME_STATE_BOARD));
    }
    /*
    private void handleGameStart(int x, String messageType, Map<String, Object> msgDetails) {
    	if (messageType.equals(GameMessage.GAME_ACTION_START)) {
            if (((String) msgDetails.get("player-white")).equals(this.userName())) {
                System.out.println("Game State: " + msgDetails.get("player-white"));
                ourPlayer = "White Player: " + this.userName;
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
                gameBoard.markPosition(translateRow(ourMove.row), translateCol(ourMove.col), translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition()),
						translateRow(ourMove.previousRow), translateCol(ourMove.previousCol), false);
              //  gameClient.sendMoveMessage(ourMove.combinedMove(translateRow(ourMove.previousRow), translateCol(ourMove.previousCol)),
                   //     ourMove.combinedMove(translateRow(ourMove.row), translateCol(ourMove.col)),
                //        ourArrow.combinedMove(translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition())));
                ourBoard.printBoard();

            } else {
                ourPlayer = "Black Player: " + this.userName();
                enemyPlayer = "White Player: " + msgDetails.get("player-white");
                ourBoard = new GameRules(false);
                search = new SearchTree(new SearchTreeNode(ourBoard));

            }
    	}
        
    } */
   

    
  private void handleGameStart(String messageType, Map<String, Object> msgDetails) {
    if (messageType.equals(GameMessage.GAME_ACTION_START)) {
        if (((String) msgDetails.get("player-white")).equals(this.userName())) {
            System.out.println("Game State: " + msgDetails.get("player-white"));
            ourPlayer = "White Player: " + this.userName;
            enemyPlayer = "Black Player: " + msgDetails.get("player-black");
        } else {
            ourPlayer = "Black Player: " + this.userName;
            enemyPlayer = "White Player: " + msgDetails.get("player-white");
        }

        // Initialize AmazonsBoard with the game state
         turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);

        // Initialize AmazonsBoard with the game state
        ArrayList<Integer> gameState = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);
        amazonsBoard.setState(gameState); // Synchronize AmazonsBoard
        gamegui.setGameState(gameState); // Update GUI
    }
} 
   private void handleOpponentMove(Map<String, Object> msgDetails) throws CloneNotSupportedException, ExecutionException {
    // Extract move details
    ArrayList<Integer> qcurr = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
    ArrayList<Integer> qnew = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
    ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);

    // Update AmazonsBoard with the opponent's move
    amazonsBoard.updateState(qcurr, qnew, arrow);

    // Update GUI
    gamegui.updateGameState(msgDetails);

    // Update internal board representation
    gameBoard.markPosition(qnew.get(0), qnew.get(1), arrow.get(0), arrow.get(1), qcurr.get(0), qcurr.get(1), true);
    amazonsBoard.updateState(qcurr, qnew, arrow); // Synchronize AmazonsBoard
    updateGameGUI(msgDetails);
    ourBoard.canEnemyMove();
    ourBoard.updateLegalQueenMoves();
    ourBoard.printBoard();

    // Check if the game is over
    if (ourBoard.goalTest()) {
        System.out.println("\n THE GAME IS NOW OVER \n");
        return;
    }

    // Make our move
    turnCount++;
    gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);

    SearchTreeNode ourBestMove = search.makeMove();
    Queen q = ourBestMove.getQueen();
    Arrow a = ourBestMove.getArrowShot();
    ourBoard.canEnemyMove();
    ourBoard.updateLegalQueenMoves();
    System.out.println("\nOur Move: [" + translateRow(q.row) + ", " + translateCol(q.col) + "]");
    gameBoard.markPosition(translateRow(q.row), translateCol(q.col), translateRow(a.getRowPosition()), translateCol(a.getColPosition()),
            translateRow(q.previousRow), translateCol(q.previousCol), false);
   amazonsBoard.updateState(qcurr, qnew, arrow); // Synchronize AmazonsBoard
    updateGameGUI(msgDetails);
    System.out.println("Our Arrow Shot: [" + translateRow(a.row) + ", " + translateCol(a.col) + "]\n");
    ourBoard.printBoard();

    // Check if the game is over again
    if (ourBoard.goalTest()) {
        System.out.println("\n THE GAME IS NOW OVER \n");
    }
}
   /* private void handleOpponentMove(, Map<String, Object> msgDetails) throws CloneNotSupportedException, ExecutionException {
    	if (gamegui != null) {
			gamegui.updateGameState(msgDetails);
		}
    	turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);
            amazonsBoard.updateGameState(msgDetails);

        System.out.println("\nOpponentMove: " + msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT));
        System.out.println("Opponent Arrow Shot: " + msgDetails.get(AmazonsGameMessage.ARROW_POS) + "\n");
        
        ArrayList<Integer> qcurr = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
        ArrayList<Integer> qnew = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
        ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
        
        Queen enemyQueen = new Queen(convertRow(qnew.get(0)), convertCol(qnew.get(1)), true);
        enemyQueen.previousRow = convertRow(qcurr.get(0));
        enemyQueen.previousCol = convertCol(qcurr.get(1));
        Arrow enemyArrow = new Arrow(convertRow(arrow.get(0)), convertCol(arrow.get(1)));
        search.makeMoveOnRoot(enemyQueen, enemyArrow);
        
        gameBoard.markPosition(qnew.get(0),qnew.get(1), arrow.get(0), arrow.get(1), qcurr.get(0), qcurr.get(1),true);
        amazonsBoard.updateGameState(qcurr, qnew, arrow); // Synchronize AmazonsBoard
        updateGameGUI(msgDetails);
        ourBoard.canEnemyMove();
        ourBoard.updateLegalQueenMoves();
        ourBoard.printBoard();
        amazonsBoard.updateGameState(msgDetails);
        // test if game is over
        if(ourBoard.goalTest()) {
            System.out.println("\n THE GAME IS NOW OVER \n");
            return;
        }
        // Our Move
        turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);
       
        SearchTreeNode ourBestMove = search.makeMove();
        Queen q = ourBestMove.getQueen();
        Arrow a = ourBestMove.getArrowShot();
        ourBoard.canEnemyMove();
        ourBoard.updateLegalQueenMoves();
        System.out.println("\nOur Move: [" + translateRow(q.row) + ", " + translateCol(q.col) + "]");
        gameBoard.markPosition(translateRow(q.row), translateCol(q.col), translateRow(a.getRowPosition()), translateCol(a.getColPosition()),
                translateRow(q.previousRow), translateCol(q.previousCol), false);
		System.out.println("Our Arrow Shot: [" + translateRow(a.row) + ", " + translateCol(a.col) + "]\n");
		ourBoard.printBoard();
        amazonsBoard.updateGameState(qcurr, qnew, arrow); // Synchronize AmazonsBoard
        updateGameGUI(msgDetails);
		//test if game is over again
		if(ourBoard.goalTest()) {
            System.out.println("\n THE GAME IS NOW OVER \n");
            
        }
        
    }
*/
   private <T extends Object> T getMessageByTag(Map<String, Object> messages, String tag) {
	    return (T) messages.get(tag);
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


	protected void move() {
	
		
	}
}
