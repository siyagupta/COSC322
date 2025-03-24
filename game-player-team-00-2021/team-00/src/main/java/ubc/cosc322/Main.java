package ubc.cosc322;

import ygraph.ai.smartfox.games.BaseGameGUI;

public class Main {
	public static void main(String[] args) {
		// Uncomment one of the following lines based on the desired player type:
		// HumanPlayer player = new HumanPlayer();
		// Spectator player = new Spectator(args[0], args[1]);
		// RandomPlayer player = new RandomPlayer();
			MonteCarloPlayer player = new MonteCarloPlayer();

		if (player.getGameGUI() == null) {
			player.Go();
		} else {
			BaseGameGUI.sys_setup();
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					player.Go();
				}
			});
		}
	}
}
