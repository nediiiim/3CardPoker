import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.util.Duration;

// Controller for ResultsScreen.fxml
public class ResultsScreenController {
	@FXML
	private Label resultTitle, resultDetail, roundWinnings, totalWinnings;
	
	@FXML
	private Button playAgainButton, exitButton;
	
	private MainClientApp mainApp;
	private PokerInfo roundInfo;
	private GameScreenController gameScreenController;
	
	// Takes all PokerInfo object and updates to the screen
	private void updateScreen() {
		String msg = roundInfo.getResultMessage().toLowerCase();
		int roundDelta = roundInfo.getRoundDelta();
		
		String title;
		String color = "-fx-text-fill: #ffffff;";
		
		if (msg.contains("did not qualify")) {
			title = "No Winner";
		} else if (msg.contains("tie")) {
			title = "Tie Game";
		} else if (roundDelta > 0) {
			title = "You Won!";
			color = "-fx-text-fill: #ffe97f";
		} else if (roundDelta < 0) {
			title = "You Lost";
			color = "-fx-text-fill: #ff6b6b;";
		} else {
			title = "";
		}
		
		resultTitle.setText(title);
		resultTitle.setStyle(color);
		
		resultDetail.setText(roundInfo.getResultMessage());
		
		int total = roundInfo.getTotalWinnings();
		
		roundWinnings.setText("Round Result: " + formatMoney(roundDelta));
		totalWinnings.setText("Total Winnings: " + formatMoney(total));
		
		// Fade in each element one at a time
		fadeInSequential(resultTitle, resultDetail, roundWinnings, totalWinnings, playAgainButton, exitButton);
	}
	
	// Allows user to play again, goes back to game screen
	@FXML
	private void playAgain() {
		gameScreenController.resetForNewRound();
		mainApp.switchToGameScreen();
	}
	
	// Exits the game
	@FXML
	private void exitGame() {
		System.exit(1);
	}
	
	// Helper function to format money for - and $
	private String formatMoney(int amount) {
		if (amount < 0) {
			return "-$" + Math.abs(amount);
		}
		return "$" + amount;
	}
	
	// Fades a sequence of nodes on at a time
	private void fadeInSequential(Node... nodes) {
		double delay = 0;
		
		for (Node node : nodes) {
			node.setOpacity(0);
			
			FadeTransition ft = new FadeTransition(Duration.millis(450), node);
			ft.setFromValue(0);
			ft.setToValue(1);
			ft.setDelay(Duration.millis(delay));
			ft.play();
			
			delay += 250;
		}
	}
	
	// Triggered if server disconnects
	// Notifies user and shuts down 5 seconds later
	public void handleServerShutdown() {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Server Stopped");
		alert.setHeaderText(null);
		alert.setContentText("Server shut down. Program will close in 5 seconds.");
		alert.show();
		
		DialogPane dialogPane = alert.getDialogPane();
		if (mainApp.newLookEnabled()) {
			dialogPane.getStylesheets().add("/styles/NewLookGameScreen.css");
		} else {
			dialogPane.getStylesheets().add("/styles/GameScreen.css");
		}
		dialogPane.getStyleClass().add("custom-alert");
		
		new Thread(() -> {
			try { Thread.sleep(5000); } catch (Exception e){}
			Platform.exit();
			System.exit(1);
		}).start();;
	}
	
	// Getters/Setters
	public void setMainApp(MainClientApp app) { this.mainApp = app; }
	public void setRoundInfo(PokerInfo info) { this.roundInfo = info; updateScreen(); }
	public void setGameScreenController(GameScreenController controller) { this.gameScreenController = controller; }
}
