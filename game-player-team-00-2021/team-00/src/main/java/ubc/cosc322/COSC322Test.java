package ubc.cosc322;

import java.util.ArrayList;
import java.util.Map;

import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * An example illustrating how to implement a GamePlayer
 * @author Yong Gao (yong.gao@ubc.ca)
 * Jan 5, 2021
 * This is the Git code
 */
public class COSC322Test extends GamePlayer{

    private SearchTree search;
	private IterativeDeepening iter;
	private GameClient gameClient = null; 
    private BaseGameGUI gamegui = null;
	
    private String userName = null;
    private String passwd = null;
    // private JFrame guiFrame = null;    
    // private GameBoard board = null; 
    private boolean gameStarted = false;   
    public String usrName = null;
    private GameRules ourBoard = null;
    int turnCount = 0;
    String ourPlayer = "";
    String enemyPlayer = "";
 
	
    /**
     * The main method
     * @param args for name and passwd (current, any string would work)
     */
    public static void main(String[] args) {				 
    	COSC322Test player = new COSC322Test("cosc322", "cosc322");
    	
    	if(player.getGameGUI() == null) {
    		player.Go();
    	}
    	else {
    		BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                	player.Go();
                }
            });
    	}
    }
	
    /**
     * Any name and passwd 
     * @param userName
      * @param passwd
     */
    public COSC322Test(String userName, String passwd) {
    	this.userName = userName;
    	this.passwd = passwd;
    	
    	//To make a GUI-based player, create an instance of BaseGameGUI
    	//and implement the method getGameGUI() accordingly
    	//this.gamegui = new BaseGameGUI(this);
    	this.gamegui= new BaseGameGUI(this);
    }
 


    @Override
    public void onLogin() {
    	System.out.println(gameClient.getRoomList());
    	gameClient.joinRoom("Kalamalka Lake");
    	System.out.println("Congratualations!!! "
    			+ "I am called because the server indicated that the login is successfully");
    	System.out.println("The next step is to find a room and join it: "
    			+ "the gameClient instance created in my constructor knows how!"); 
    	
    	userName=gameClient.getUserName();
    	if(gamegui!=null)
    	{
    	gamegui.setRoomInformation(gameClient.getRoomList());
    	}
    	
    }

    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
		if(messageType.equals(GameMessage.GAME_ACTION_START)){
			if(((String) msgDetails.get("player-white")).equals(this.userName())){
				System.out.println("Game State: " +  msgDetails.get("player-white"));
				ourPlayer = "White Player: " + this.userName();
				enemyPlayer = "Black Player: " + msgDetails.get("player-black");
                turnCount++;
                gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " +enemyPlayer);
                ourBoard = new GameRules(true);
                System.out.println("Initial Board");
                ourBoard.printBoard();
				ourBoard.canEnemyMove();
				ourBoard.updateLegalQueenMoves();
                search = new SearchTree(new SearchTreeNode(ourBoard));
				iter = new IterativeDeepening();
                SearchTreeNode ourBestMove = null;
                            try {
                                ourBestMove = search.makeMove();
                            } catch (ExecutionException ex) {
                            }
                Queen ourMove = ourBestMove.getQueen();
                Arrow ourArrow = ourBestMove.getArrowShot();
                ourBoard.canEnemyMove();
                ourBoard.updateLegalQueenMoves();
				 System.out.println("\nOur Move: [" + translateRow(ourMove.row) + ", " + translateCol(ourMove.col) + "]");
				 System.out.println("Our Arrow Shot: [" + translateRow(ourArrow.row) + ", " + translateCol(ourArrow.col) + "]\n");
				//  gameClient.markPosition(translateRow(ourMove.row), translateCol(ourMove.col), translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition()),
				//  		translateRow(ourMove.previousRow), translateCol(ourMove.previousCol), false);

                //  gameClient.sendMoveMessage(ourMove.combinedMove(translateRow(ourMove.previousRow), translateCol(ourMove.previousCol)),
                //          ourMove.combinedMove(translateRow(ourMove.row), translateCol(ourMove.col)),
                //          ourArrow.combinedMove(translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition())));
                ourBoard.printBoard();

            }
			else {
                ourPlayer = "Black Player: " + this.userName();
                enemyPlayer = "White Player: " + msgDetails.get("player-white");
                ourBoard = new GameRules(false);
                search = new SearchTree(new SearchTreeNode(ourBoard));
				

            }
			
		}
		else if(messageType.equals(GameMessage.GAME_ACTION_MOVE)){

        	// try {
			// 	handleOpponentMove(msgDetails);
			// } catch (CloneNotSupportedException e) {
			// 	e.printStackTrace();
			// }
        }
		//return true;
    	//This method will be called by the GameClient when it receives a game-related message
    	//from the server.
    	System.out.println(msgDetails);
    	ArrayList<Integer> GameS= (ArrayList<Integer>)msgDetails.get("game-state");
    	gamegui.setGameState(GameS);
	
    	//For a detailed description of the message types and format, 
    	//see the method GamePlayer.handleGameMessage() in the game-client-api document. 
    	    	
    	return true;   	
    }

	//handle the event that the opponent makes a move. 
	private void handleOpponentMove(Map<String, Object> msgDetails) throws CloneNotSupportedException, ExecutionException{
        boolean gameOver = false;
        turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);
		// System.out.println("\nOpponentMove: " + msgDetails.get(AmazonsGameMessage.Queen_POS_NEXT));
        // System.out.println("Opponent Arrow Shot: " + msgDetails.get(AmazonsGameMessage.ARROW_POS) + "\n");
		// ArrayList<Integer> qcurr = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
		// ArrayList<Integer> qnew = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.Queen_POS_NEXT);
		// ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
        // Enemy move
		// Queen enemyQueen = new Queen(convertRow(qnew.get(0)), convertCol(qnew.get(1)), true);
		// enemyQueen.previousRow = convertRow(qcurr.get(0));
		// enemyQueen.previousCol = convertCol(qcurr.get(1));
		// Arrow enemyArrow = new Arrow(convertRow(arrow.get(0)), convertCol(arrow.get((1))));
		// search.makeMoveOnRoot(enemyQueen, enemyArrow);
		//iter.makeMoveOnRoot(enemyQueen, enemyArrow);
        // board.markPosition(qnew.get(0), qnew.get(1), arrow.get(0), arrow.get(1),
        //         qcurr.get(0), qcurr.get(1), true);
        ourBoard.canEnemyMove();
		ourBoard.updateLegalQueenMoves();
        ourBoard.printBoard();

        // Check if we're at a goal node
        gameOver = ourBoard.goalTest();

        if(gameOver) {
            System.out.println("\n THE GAME IS NOW OVER \n");
        }

        // Our move
        turnCount++;
        gamegui.setTitle("Turn: " + turnCount + " | Move: " + userName() + " | " + ourPlayer + " | " + enemyPlayer);
        SearchTreeNode ourBestMove = search.makeMove();
        Queen ourMove = ourBestMove.getQueen();
        Arrow ourArrow = ourBestMove.getArrowShot();
        ourBoard.canEnemyMove();
        ourBoard.updateLegalQueenMoves();
        System.out.println("\nOur Move: [" + translateRow(ourMove.row) + ", " + translateCol(ourMove.col) + "]");
        // gameClient.markPosition(translateRow(ourMove.row), translateCol(ourMove.col), translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition()),
        //         translateRow(ourMove.previousRow), translateCol(ourMove.previousCol), false);
		// System.out.println("Our Arrow Shot: [" + translateRow(ourArrow.row) + ", " + translateCol(ourArrow.col) + "]\n");
        // gameClient.sendMoveMessage(ourMove.combinedMove(translateRow(ourMove.previousRow), translateCol(ourMove.previousCol)),
				// ourMove.combinedMove(translateRow(ourMove.row), translateCol(ourMove.col)),
				// ourArrow.combinedMove(translateRow(ourArrow.getRowPosition()), translateCol(ourArrow.getColPosition())));
        ourBoard.printBoard();
        gameOver = ourBoard.goalTest();

        if(gameOver) {
            System.out.println("\n THE GAME IS NOW OVER \n");
        }
	}
    
	private int convertRow(int row){
        return Math.abs(row - 10);	// formula to convert server's row coordinate system to our Board's coordinate system
    }

    private int convertCol(int col){
        return (col - 1);	      // formula to convert server's column coordinate system to our Board's coordinate system
    }

    private int translateCol(int col){
        return (col + 1);	      // formula to translate our Board's column coordinate system to the server's coordinate system
    }

    private int translateRow(int row){
        return Math.abs(10 - row);	      // formula to convert our Board's row coordinate system to the server's coordinate system
    }



    
    @Override
    public String userName() {
    	return userName;
    }

	@Override
	public GameClient getGameClient() {
		// TODO Auto-generated method stub
		return this.gameClient;
	}

	@Override
	public BaseGameGUI getGameGUI() {
		// TODO Auto-generated method stub
		return  this.gamegui;
	}

	@Override
	public void connect() {
		// TODO Auto-generated method stub
    	gameClient = new GameClient(userName, passwd, this);			
	}

 
}//end of class
