import java.util.ArrayList;
import edu.princeton.cs.introcs.*;

public class SkunkController
{
	private static final int DOUBLE_SKUNK_SUM = 2;
	private static final int SKUNK_DEUCE_SUM = 3;
	public SkunkUI skunkUI;
	public UI ui;
	public int numberOfPlayers;
	public String[] playerNames;
	public ArrayList<Player> players;
	public int kitty;

	public Player activePlayer;
	public int activePlayerIndex;

	public boolean wantsToQuit;
	public boolean oneMoreRoll;

	public Dice skunkDice;

	public SkunkController(SkunkUI ui)
	{
		this.skunkUI = ui;
		this.ui = ui; // hide behind the interface UI
		
		this.playerNames = new String[20];
		this.players = new ArrayList<Player>();
		this.skunkDice = new Dice();
		this.wantsToQuit = false;
		this.oneMoreRoll = false;
	}

	public boolean run()
	{
		ui.println("Welcome to Skunk 0.48\n");

		String numberPlayersString = skunkUI.promptReadAndReturn("How many players?");
		this.numberOfPlayers = Integer.parseInt(numberPlayersString);

		for (int playerNumber = 0; playerNumber < numberOfPlayers; playerNumber++)
		{
			ui.print("Enter name of player " + (playerNumber + 1) + ": ");
			playerNames[playerNumber] = StdIn.readLine();
			this.players.add(new Player(50));
		}
		activePlayerIndex = 0;
		activePlayer = players.get(activePlayerIndex);

		ui.println("Starting game...\n");
		boolean gameNotOver = true;

		while (gameNotOver)
		{
			ui.println("Next player is " + playerNames[activePlayerIndex] + ".");
			activePlayer.setTurnScore(0);
			
			rollingTurn();
			
			if (activePlayer.getGameScore() >= 100)
				gameNotOver = false;

			printScoreboard();

			ui.println("Turn passes to right...");

			activePlayerIndex = (activePlayerIndex + 1) % numberOfPlayers;
			activePlayer = players.get(activePlayerIndex);

		}
		// last round: everyone but last activePlayer gets another shot

		ui.println("**** Last turn for all... ****");

		for (int i = activePlayerIndex, count = 0; count < numberOfPlayers-1; i = (i++) % numberOfPlayers, count++)
		{
			ui.println("Last turn for player " + playerNames[activePlayerIndex] + "...");
			activePlayer.setTurnScore(0);

			rollingTurn();

			activePlayer.setTurnScore(activePlayer.getRollScore() + skunkDice.getLastRoll());
			ui.println("Final roll of " + skunkDice.toString() + ", giving final game score of " + activePlayer.getGameScore());

		}

		determineWinner();

		printScoreboard();
		return true;
	}

	private void rollingTurn() {
		boolean wantsToRoll = getRollChoice(); 
		
		while (wantsToRoll)
		{
			activePlayer.setRollScore(0);
			skunkDice.roll();
			if (isDoubleSkunk())
			{
				ui.println("Two Skunks! You lose the turn, zeroing out both turn and game scores and paying 4 chips to the kitty");
				kitty += 4;
				activePlayer.scoreSkunkRoll(4);
				activePlayer.setGameScore(0);
				wantsToRoll = false;
				break;
			}
			else if (isSkunkDeuce())
			{
				ui.println(
						"Skunks and Deuce! You lose the turn, zeroing out the turn score and paying 2 chips to the kitty");
				kitty += 2;
				activePlayer.scoreSkunkRoll(2);
				wantsToRoll = false;
				break;
			}
			else if (isRegularSkunk())
			{
				ui.println("One Skunk! You lose the turn, zeroing out the turn score and paying 1 chip to the kitty");
				kitty += 1;
				activePlayer.scoreSkunkRoll(1);
				wantsToRoll = false;
				break;
			}

			activePlayer.setRollScore(skunkDice.getLastRoll());
			activePlayer.setTurnScore(activePlayer.getTurnScore() + skunkDice.getLastRoll());
			ui.println("Roll of " + skunkDice.toString() + ", gives new turn score of " + activePlayer.getTurnScore());

			wantsToRoll = getRollChoice();
		}
		endTurn();
	}

	private void determineWinner() {
		int winner = 0;
		int winnerScore = 0;

		for (int player = 0; player < numberOfPlayers; player++)
		{
			Player nextPlayer = players.get(player);
			ui.println("Final game score for " + playerNames[player] + " is " + nextPlayer.getGameScore());
			if (nextPlayer.getGameScore() > winnerScore)
			{
				winner = player;
				winnerScore = nextPlayer.getGameScore();
			}
		}

		ui.println(
				"Game winner is " + playerNames[winner] + " with score of " + players.get(winner).getGameScore());
		players.get(winner).setNumberChips(players.get(winner).getNumberChips() + kitty);
		ui.println("Game winner earns " + kitty + " chips , finishing with " + players.get(winner).getNumberChips());
	}

	private void printScoreboard() {
		ui.println("Scoreboard: ");
		ui.println("Kitty has " + kitty + " chips.");
		ui.println("Player name -- Turn score -- Game score -- Total chips");
		ui.println("-----------------------");

		for (int i = 0; i < numberOfPlayers; i++)
		{
			ui.println(playerNames[i] + " -- " + players.get(i).getTurnScore() + " -- " + players.get(i).getGameScore()
					+ " -- " + players.get(i).getNumberChips());
		}
		ui.println("-----------------------");
	}

	private void endTurn() {
		ui.println("End of turn for " + playerNames[activePlayerIndex]);
		ui.println("Score for this turn is " + activePlayer.getTurnScore() + ", added to...");
		ui.println("Previous game score of " + activePlayer.getGameScore());
		activePlayer.setGameScore(activePlayer.getGameScore() + activePlayer.getTurnScore());
		ui.println("Gives new game score of " + activePlayer.getGameScore());
		ui.println("");
	}

	private boolean isDoubleSkunk() 
	{
		return skunkDice.getLastRoll() == DOUBLE_SKUNK_SUM;
	}

	private boolean isSkunkDeuce() 
	{
		return skunkDice.getLastRoll() == SKUNK_DEUCE_SUM;
	}

	private boolean isRegularSkunk()
	{
		return skunkDice.getDie1().getLastRoll() == 1 || skunkDice.getDie2().getLastRoll() == 1;
	}

	private boolean getRollChoice()
	{
		String wantsToRollStr = ui.promptReadAndReturn("Roll? y or n");
		return 'y' == wantsToRollStr.toLowerCase().charAt(0);
	}

}
