import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Entry point for client-side, loads all FXML screens with css
public class MainClientApp extends Application{
	private Stage primaryStage;
	private Scene welcomeScene, gameScene, resultsScene;
	private static WelcomeController welcomeController;
	private static GameScreenController gameScreenController;
	private static ResultsScreenController resultsScreenController;
	private boolean newLook = false;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	// Loads all screens, sets references, and displays welcome screen
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.setTitle("3 Card Poker (Client)");
		
		try {
			// Load Welcome screen
			FXMLLoader welcomeLoader = new FXMLLoader(getClass().getResource("/FXML/Welcome.fxml"));
			welcomeScene = new Scene(welcomeLoader.load());
			welcomeController = welcomeLoader.getController();
			welcomeController.setMainApp(this);
			
			// Load Game screen
			FXMLLoader gameScreenLoader = new FXMLLoader(getClass().getResource("/FXML/GameScreen.fxml"));
			gameScene = new Scene(gameScreenLoader.load());
			gameScreenController = gameScreenLoader.getController();
			gameScreenController.setMainApp(this);
			
			// Load Results screen
			FXMLLoader resultsScreenLoader = new FXMLLoader(getClass().getResource("/FXML/ResultsScreen.fxml"));
			resultsScene = new Scene(resultsScreenLoader.load());
			resultsScreenController = resultsScreenLoader.getController();
			resultsScreenController.setMainApp(this);
			resultsScreenController.setGameScreenController(gameScreenController);
			
			// Apply CSS
			welcomeScene.getStylesheets().add("/styles/Welcome.css");
			gameScene.getStylesheets().add("/styles/GameScreen.css");
			resultsScene.getStylesheets().add("/styles/ResultsScreen.css");
			
			primaryStage.setScene(welcomeScene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	// Switches to welcome screen
	public void switchToWelcomeScreen() {
		primaryStage.setScene(welcomeScene);
	}
	
	// Switches to game screen
	public void switchToGameScreen() {
		gameScreenController.setClient(welcomeController.getClient());
		primaryStage.setScene(gameScene);
	}
	
	// Switches to results screen
	public void switchToResultsScreen(PokerInfo info) {
		resultsScreenController.setRoundInfo(info);
		primaryStage.setScene(resultsScene);
	}
	
	// Switches theme (between default and new look winter theme)
	public void toggleTheme() {
		newLook = !newLook;
		
		gameScene.getStylesheets().clear();
		resultsScene.getStylesheets().clear();
		
		if (newLook) {
			gameScene.getStylesheets().add("/styles/NewLookGameScreen.css");
			resultsScene.getStylesheets().add("/styles/NewLookResultsScreen.css");
		} else {
			gameScene.getStylesheets().add("/styles/GameScreen.css");
			resultsScene.getStylesheets().add("/styles/ResultsScreen.css");
		}
	}
	
	// Getters/Setters
	public GameScreenController getGameScreenController() { return gameScreenController; }
	public ResultsScreenController getResultsScreenController() { return resultsScreenController; }
	public boolean newLookEnabled() { return newLook; }
}
