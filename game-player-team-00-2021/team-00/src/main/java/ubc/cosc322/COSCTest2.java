package ubc.cosc322;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GamePlayer;



public class COSCTest2 extends GamePlayer {

    private GameClient gameClient = null;
    private BaseGameGUI gamegui = null;
   // private static PlayerMoveHandler playerMoveHandler; // New handler for player movement
    private SearchTree search;

    private String userName = null;
    private String passwd = null;

    //added for bot 
    private int[][] boardState = new int[10][10];
    private int playerColor = 0; // 1 = black, 2 = white
 //   private final minimax bot = new minimax();
     

  public static void main(String[] args) {
	  
      COSC322Test player = new COSC322Test("cosc322", "cosc322");

        if (player.getGameGUI() == null) {
            player.Go();
        } else {
            BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(() -> player.Go());
        }
        
        //playerMoveHandler.Timer();
    }

   
     

  public COSCTest2(String userName, String passwd) {
      this.userName = userName;
      this.passwd = passwd;
      this.gamegui = new BaseGameGUI(this);

        // Initialize player movement handler
        //this.playerMoveHandler = new PlayerMoveHandler(this);
    }

    
     

  public void onLogin() {
      //gameClient.joinRoom("Okanagan Lake");
      userName = gameClient.getUserName();
      if (gamegui != null) {
          gamegui.setRoomInformation(gameClient.getRoomList());}}



@Override
public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
    switch (messageType) {

        case "cosc322.game-state.board":
            ArrayList<Integer> gameState = getMessageByTag(msgDetails, "game-state");

            // Convert 1D gameState into 2D board array
            boardState = convertGameStateTo2DArray(gameState);
            gamegui.setGameState(gameState); // Visual update
            break;

        case "cosc322.game-action.move":
            ArrayList<Integer> queenFrom = getMessageByTag(msgDetails, "queen-position-current");
            ArrayList<Integer> queenTo   = getMessageByTag(msgDetails, "queen-position-next");
            ArrayList<Integer> arrow     = getMessageByTag(msgDetails, "arrow-position");
            System.out.println(arrow);
            ArrayList<Integer> queenFromLocal =   new  ArrayList<Integer>();
            ArrayList<Integer> queenToLocal =   new  ArrayList<Integer>();
            ArrayList<Integer> arrowLocal =   new  ArrayList<Integer>();
            queenFromLocal.add(queenFrom.get(0));
            queenFromLocal.add(queenFrom.get(1) -1);
            queenToLocal.add(queenTo.get(0));
            queenToLocal.add(queenTo.get(1) -1);
            arrowLocal.add(arrow.get(0));
            arrowLocal.add(arrow.get(1) -1);
            applyMove(queenFromLocal, queenToLocal, arrowLocal);
            gamegui.updateGameState(queenFrom, queenTo, arrow);
            playIfMyTurn();
            break;

        case "cosc322.game-action.start":
            String whitePlayer = getMessageByTag(msgDetails, "player-white");

            // Assume local username was saved in `this.username` earlier
            playerColor = whitePlayer.equalsIgnoreCase(gameClient.getUserName()) ? 1 : 2;

            System.out.println("*** I am " + (playerColor == 1 ? "White" : "Black") + " ***");

            if (playerColor == 2) {
                playIfMyTurn(); // black goes first
            }
            break;
           
    }

    return true;
}

private int[][] convertGameStateTo2DArray(ArrayList<Integer> gameState) {
    int[][] board = new int[10][10];
    for (int i = 1; i <= 10; i++) { // Skip the first row of padding
        for (int j = 1; j <= 10; j++) { // Skip the first column of padding
            board[i - 1][j - 1] = gameState.get(i * 11 + j); // Map to 10x10 board
        }
    }
    return board;
}


    



    private void updateBoardFromList(ArrayList<Integer> state) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                boardState[i][j] = state.get((10 - i) * 11 + j + 1); // Skip first padding row and col
                if (boardState[i][j] == 3) boardState[i][j] = 3;
                else if (boardState[i][j] == 1) boardState[i][j] = 1;
                else if (boardState[i][j] == 2) boardState[i][j] = 2;
                else boardState[i][j] = 0;
            }
        }
    }

    private void applyMove(ArrayList<Integer> from, ArrayList<Integer> to, ArrayList<Integer> arrow) {
        int fr = 10 - from.get(0);  // Convert row to 0-based index from top
        int fc = from.get(1);       // Column is already 0-based
        int tr = 10 - to.get(0);
        int tc = to.get(1);
        int ar = 10 - arrow.get(0);
        int ac = arrow.get(1);
    
        // ✅ Sanity check to avoid crashing from bad input
        if (isOutOfBounds(fr, fc) || isOutOfBounds(tr, tc) || isOutOfBounds(ar, ac)) {
            System.err.printf("❌ Invalid move: from (%d,%d) to (%d,%d), arrow (%d,%d)%n",
                    fr, fc, tr, tc, ar, ac);
            return;
        }
    
        // 👑 Move queen
        int queen = boardState[fr][fc];
        boardState[fr][fc] = 0;
        boardState[tr][tc] = queen;
    
        // 🏹 Shoot arrow
        boardState[ar][ac] = 3;
    
        // 🧠 Log board after move
        System.out.println("Updated board:");
        for (int[] row : boardState) {
            System.out.println(Arrays.toString(row));
        }
    }
    
    // ✅ Utility to ensure we're not accessing out-of-bound positions
    private boolean isOutOfBounds(int r, int c) {
        return r < 0 || r >= 10 || c < 0 || c >= 10;
    }
    
    
    

    private void playIfMyTurn()  {
        int myQueens = 0, enemyQueens = 0;
        int myColor = playerColor;
        int enemyColor = (myColor == 1) ? 2 : 1;
    
        // Count how many queens each side has
        for (int[] row : boardState) {
            for (int cell : row) {
                if (cell == myColor) myQueens++;
                else if (cell == enemyColor) enemyQueens++;
            }
        }
    
        // Decide if it's our turn
        boolean isMyTurn = (myColor == 1 && myQueens == enemyQueens)
                        || (myColor == 2 && myQueens == 4 && enemyQueens == 4);
    
        if (!isMyTurn) return;
        
        SearchTreeNode ourBestMove;
		try {
			ourBestMove = search.makeMove();
		
        Queen q = ourBestMove.getQueen();
        Arrow a = ourBestMove.getArrowShot();
        
        int[] move = new int[6];
        move[0] = q.getOldPosition()[0];
        move[1] = q.getOldPosition()[1];
        move[2] = q.getNewPosition()[0];
        move[3] = q.getNewPosition()[1];
        move[4] = a.getRowPosition();
        move [5] =   a.getColPosition();
        
       // SmartFox-style coordinates (row: 1-10 from bottom up, col: 0-9)
        ArrayList<Integer> queenFrom = convertToGameCoords(move[0], move[1]);
        ArrayList<Integer> queenTo   = convertToGameCoords(move[2], move[3]);
        ArrayList<Integer> arrow     = convertToGameCoords(move[4], move[5]);
        ArrayList<Integer> queenFromGUI = convertToGameCoordsGUI(move[0], move[1]);
        ArrayList<Integer> queenToGUI   = convertToGameCoordsGUI(move[2], move[3]);
        ArrayList<Integer> arrowGUI     = convertToGameCoordsGUI(move[4], move[5]);
    
        System.out.println("Applying move:");
        System.out.printf("Queen from %s to %s, arrow at %s%n",
                queenFrom.toString(), queenTo.toString(), arrow.toString());
    
        // Send move to server
        gameClient.sendMoveMessage(queenFromGUI, queenToGUI, arrowGUI);
    
        // Apply move to local board (converted to array indexing)
        applyMove(queenFrom, queenTo, arrow);
    
        // Show updated move in GUI (pass original SmartFox-style coords)
        gamegui.updateGameState(queenFromGUI, queenToGUI, arrowGUI);
        } catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    private ArrayList<Integer> convertToGameCoords(int row, int col) {
        ArrayList<Integer> coords = new ArrayList<>();
        coords.add(10 - row); // flip row
        coords.add(col);
        return coords;
    }

    private ArrayList<Integer> convertToGameCoordsGUI(int row, int col) {
        ArrayList<Integer> coords = new ArrayList<>();
        coords.add(10 - row); // flip row
        coords.add(col + 1);
        return coords;
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

    @SuppressWarnings("unchecked")
    private <T> T getMessageByTag(Map<String, Object> msgDetails, String tag) {
        return (T) msgDetails.get(tag);
    }

}