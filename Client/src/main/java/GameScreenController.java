import java.io.Serializable;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

// Controller for GameScreen.fxml
public class GameScreenController {
	@FXML
	private MenuBar menuBar;
	
	@FXML
	private Menu optionsMenu;
	
	@FXML
	private MenuItem exit, freshStart, newLook;
	
	@FXML
	private Label dealerLabel, playerLabel, totalWinningsLabel;
	
	@FXML
	private HBox dealerCards, playerCards;
	
	@FXML
	private Button deal, play, fold;
	
	@FXML
	private ListView<String> gameLog;
	
	@FXML
	private TextField anteBet, pairPlus, playBet;
	
	private Hand playerHand, dealerHand;

	private boolean lockNextAnte = false;
	private int lockedAnteAmount = 0;
	
	private Client client;
	private MainClientApp mainApp;
	
	// Called after FXML loads
	@FXML
	public void initialize() {
		play.setDisable(true);
		fold.setDisable(true);
		deal.setDisable(false);
		
		playBet.setDisable(true);
		
		setUpInitialCardImages();
		
		exit.setOnAction(e -> exitFunc());
		freshStart.setOnAction(e -> freshStartFunc());
		newLook.setOnAction(e -> newLookFunc());
		
		anteBet.setText("5");
		pairPlus.setText("0");
		
		addToGameLog("Welcome to 3 Card Poker! Enter your bets to begin");
	}
	
	// Helper function to load back of card images in dealer and players box
	private void setUpInitialCardImages() {
		// Remove everything in both boxes
		dealerCards.getChildren().clear();
		playerCards.getChildren().clear();
		
		// Loops 3 times (each player gets 3 cards
		for (int i = 0; i < 3; i++) {
			ImageView dealerCard = new ImageView("/CardImages/back.png");
			dealerCard.setFitHeight(120);
			dealerCard.setFitWidth(80);
			dealerCards.getChildren().add(dealerCard);
			
			ImageView playerCard = new ImageView("/CardImages/back.png");
			playerCard.setFitHeight(120);
			playerCard.setFitWidth(80);
			playerCards.getChildren().add(playerCard);
		}
	}
	
	@FXML
	// Deal button functionality
	private void dealFunc(ActionEvent event) {
		String anteText = anteBet.getText().trim();
		String pairPlusText = pairPlus.getText().trim();
		
		if (anteText.isEmpty()) {
			showAlert("Missing Bet", "Please enter your ante bet before dealing.");
			return;
		}
		
		if (pairPlusText.isEmpty()) {
			showAlert("Missing Bet", "Please enter your pair plus bet before dealing. (Enter 0 if you don't want to place a pair plus bet)");
			return;
		}
		
		int anteValue;
		try {
			anteValue = Integer.parseInt(anteText);
		} catch (Exception e) {
			showAlert("Invalid Bet", "Ante bet must be a valid number.");
			return;
		}
		
		if (anteValue < 5 || anteValue > 25) {
			showAlert("Invalid Bet", "Ante bet must be between $5 and $25.");
			return;
		}
		
		int pairPlusValue;
		try {
			pairPlusValue = Integer.parseInt(pairPlusText);
		} catch (Exception e) {
			showAlert("Invalid Bet", "Pair Plus bet must be a valid number.");
			return;
		}
		
		if (pairPlusValue != 0 && (pairPlusValue < 5 || pairPlusValue > 25)) {
			showAlert("Invalid Bet", "Pair Plus bet must be $0 or between $5 and $25.");
			return;
		}
		
		addToGameLog("Bets placed = Ante: $" + anteValue + ", Pair Plus: $" + pairPlusValue);
		
		deal.setDisable(true);
		play.setDisable(true);
		fold.setDisable(true);
		
		anteBet.setDisable(true);
		pairPlus.setDisable(true);
		
		// Build request to send to server
		PokerInfo request = new PokerInfo("START_GAME");
		request.setAnteBet(anteValue);
		request.setPairPlusBet(pairPlusValue);
		client.send(request);
	}
	
	@FXML
	// Play button functionality
	private void playFunc(ActionEvent event) {
		PokerInfo request = new PokerInfo("PLAY");
		client.send(request);
		
		play.setDisable(true);
		fold.setDisable(true);
		
		playBet.setText(anteBet.getText());
	}
	
	@FXML
	// Fold button functionality
	private void foldFunc(ActionEvent event) {
		play.setDisable(true);
		fold.setDisable(true);
		
		PokerInfo request = new PokerInfo("FOLD");
		request.setAnteBet(Integer.parseInt(anteBet.getText()));
		request.setPairPlusBet(Integer.parseInt(pairPlus.getText()));
		client.send(request);
	}
	
	// Flips all three players cards one by one
	private void flipPlayerCards(Hand playerHand) {
		ImageView view1 = (ImageView) playerCards.getChildren().get(0);
		ImageView view2 = (ImageView) playerCards.getChildren().get(1);
		ImageView view3 = (ImageView) playerCards.getChildren().get(2);
		
		Card card1 = playerHand.getCards().get(0);
		Card card2 = playerHand.getCards().get(1);
		Card card3 = playerHand.getCards().get(2);
		
		String image1 = "/CardImages/" + cardToImageName(card1);
		String image2 = "/CardImages/" + cardToImageName(card2);
		String image3 = "/CardImages/" + cardToImageName(card3);
		
		flipSingleCard(view1, image1, 0);
		flipSingleCard(view2, image2, 750);
		flipSingleCard(view3, image3, 1500);
		
		PauseTransition afterFlipDelay = new PauseTransition(Duration.millis(1900));
		afterFlipDelay.setOnFinished(e -> {
			play.setDisable(false);
			fold.setDisable(false);		
			addToGameLog("Choose to play or fold");
		});
		afterFlipDelay.play();

	}
	
	// Flips all three dealer cards
	private void flipDealerCards(Hand dealerHand) {
		ImageView view1 = (ImageView) dealerCards.getChildren().get(0);
		ImageView view2 = (ImageView) dealerCards.getChildren().get(1);
		ImageView view3 = (ImageView) dealerCards.getChildren().get(2);
		
		Card card1 = dealerHand.getCards().get(0);
		Card card2 = dealerHand.getCards().get(1);
		Card card3 = dealerHand.getCards().get(2);
		
		String image1 = "/CardImages/" + cardToImageName(card1);
		String image2 = "/CardImages/" + cardToImageName(card2);
		String image3 = "/CardImages/" + cardToImageName(card3);
		
		flipSingleCard(view1, image1, 0);
		flipSingleCard(view2, image2, 750);
		flipSingleCard(view3, image3, 1500);
	}
	
	// Helper function to display pop up
	private void showAlert(String title, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		
		// Load style sheet
		DialogPane dialogPane = alert.getDialogPane();
		if (mainApp.newLookEnabled()) {
			dialogPane.getStylesheets().add("/styles/NewLookGameScreen.css");
		} else {
			dialogPane.getStylesheets().add("/styles/GameScreen.css");
		}
		dialogPane.getStyleClass().add("custom-alert");
		
		alert.showAndWait();
	}
	
	// Helper function to add to game log
	private void addToGameLog(String message) {
		gameLog.getItems().add(message);
		
		if (gameLog.getItems().size() > 9) {
			Platform.runLater(() -> {
				gameLog.scrollTo(gameLog.getItems().size() - 1);
			});
		}
	}
	
	// Handle Exit button in menu
	private void exitFunc() {
		System.exit(1);
	}
	
	// Handle Fresh Start button in menu
	private void freshStartFunc() {
		PokerInfo request = new PokerInfo("RESET_GAME");
		client.send(request);

		lockNextAnte = false;
		lockedAnteAmount = 0;
		
		anteBet.setText("5");
		pairPlus.setText("0");
		playBet.clear();
		anteBet.setDisable(false);
		pairPlus.setDisable(false);
		
		deal.setDisable(false);
		play.setDisable(true);
		fold.setDisable(true);
		
		setUpInitialCardImages();
		
		playerHand = null;
		dealerHand = null;
		
		totalWinningsLabel.setText("Total Winnings: $0");
	}
	
	// Handle New Look button in menu
	private void newLookFunc() {
		mainApp.toggleTheme();
		addToGameLog("Theme changed.");
	}
	
	// Helper function to covert card info to file name to grab image
	private String cardToImageName(Card c) {
		String valueName;
		
		switch (c.getValue()) {
			case 14: 
				valueName = "ace";
				break;
			case 13: 
				valueName = "king";
				break;
			case 12: 
				valueName = "queen";
				break;
			case 11: 
				valueName = "jack";
				break;
			default: 
				valueName = String.valueOf(c.getValue());
		}
		
		String suitName = c.getSuit().toLowerCase();
		
		return valueName + "_of_" + suitName + ".png";
	}
	
	// Helper function to flip a card
	private void flipSingleCard(ImageView view, String newPath, int delayMillis) {
		ScaleTransition shrink = new ScaleTransition(Duration.millis(300), view);
		shrink.setFromX(1);
		shrink.setToX(0);
		
		ScaleTransition expand = new ScaleTransition(Duration.millis(300), view);
		expand.setFromX(0);
		expand.setToX(1);
		
		shrink.setOnFinished(e -> {
			view.setImage(new Image(newPath));
			expand.play();
		});
		
		PauseTransition delay = new PauseTransition(Duration.millis(delayMillis));
		delay.setOnFinished(e -> shrink.play());
		delay.play();
	}
	
	// Main message dispatcher for server
	public void handleServerMessage(Serializable data) {
		if (!(data instanceof PokerInfo)) return;
		
		PokerInfo info = (PokerInfo) data;
		String type = info.getMessageType();
		
		if ("ROUND_STARTED".equals(type)) {
			playerHand = info.getPlayerHand();
			dealerHand = info.getDealerHand();
			
			flipPlayerCards(playerHand);
		}
		
		if ("ROUND_RESULT".equals(info.getMessageType())) {
			// Checks if dealer failed to qualify and push ante bet
			String resultMsg = info.getResultMessage();
			if (resultMsg != null && resultMsg.contains("Dealer did not qualify")) {
				try {
					lockedAnteAmount = Integer.parseInt(anteBet.getText().trim());
					lockNextAnte = lockedAnteAmount > 0;
				} catch (Exception e) {
					lockNextAnte = false;
					lockedAnteAmount = 0;
				}
			} else {
				lockNextAnte = false;
				lockedAnteAmount = 0;
			}

			flipDealerCards(dealerHand);
			
			PauseTransition afterDealerFlip = new PauseTransition(Duration.millis(3500));
			afterDealerFlip.setOnFinished(e -> {
				totalWinningsLabel.setText("Total Winnings: " + formatMoney(info.getTotalWinnings()));
				addToGameLog(info.getResultMessage());
				mainApp.switchToResultsScreen(info);
			});
			
			afterDealerFlip.play();
		}
		
		if ("RESET_CONFIRM".equals(type)) {
			totalWinningsLabel.setText("Total Winnings: $" + info.getTotalWinnings());
			
			gameLog.getItems().clear();
			addToGameLog("Game successfully reset. Enter your bets to begin.");
			
			return;
		}
		
		if ("SERVER_SHUTDOWN".equals(data.toString())) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Server Stopped");
			alert.setHeaderText(null);
			alert.setContentText("Server shut down. Program will close in 5 seconds.");
			
			DialogPane dialogPane = alert.getDialogPane();
			if (mainApp.newLookEnabled()) {
				dialogPane.getStylesheets().add("/styles/NewLookGameScreen.css");
			} else {
				dialogPane.getStylesheets().add("/styles/GameScreen.css");
			}
			dialogPane.getStyleClass().add("custom-alert");
			
			alert.show();
			
			new Thread(() -> {
				try { Thread.sleep(5000); } catch (Exception e){}
				Platform.exit();
				System.exit(1);
			}).start();;
		}
	}
	
	// Resets the game screen to get ready for next round
	public void resetForNewRound() {
		pairPlus.setDisable(false);
		pairPlus.setText("0");

		playBet.clear();

		if (lockNextAnte && lockedAnteAmount > 0) {
			anteBet.setText(String.valueOf(lockedAnteAmount));
			anteBet.setDisable(true);

			addToGameLog("Dealer did not qualify last round.\nAnte is locked at $" + lockedAnteAmount + " for this round.");
		} else {
			anteBet.setText("5");
			anteBet.setDisable(false);

			addToGameLog("New round started! Enter your bets to begin.");
		}
		
		deal.setDisable(false);
		
		setUpInitialCardImages();
		
		playerHand = null;
		dealerHand = null;
	}
	
	// Helper function to format money correctly with negatives and $
	private String formatMoney(int amount) {
		if (amount < 0) {
			return "-$" + Math.abs(amount);
		}
		return "$" + amount;
	}
	
	// Getters/Setters
	public void setMainApp(MainClientApp app) { this.mainApp = app; }
	public Client getClient() { return client; }
	public void setClient(Client client) { 
		this.client = client; 
		
		client.setCallback(data -> {
			Platform.runLater(() -> {
				if ("SERVER_SHUTDOWN".equals(data.toString())) {
					mainApp.getResultsScreenController().handleServerShutdown();
					return;
				}
				handleServerMessage(data);
			});
		});
	}
}
