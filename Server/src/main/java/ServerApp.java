import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Class launches server application, initializes and switches between server GUI scenes
public class ServerApp extends Application {
	private Stage primaryStage;
	private Scene introScene, gameStateScene;
	private static ServerIntroController introController;
	private static ServerGameStateController gameStateController;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	// Sets up window, loads both FXML files, attaches CSS files, and displays Intro screen
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.setTitle("3 Card Poker (Server)");
		
		try {
			// Load FXML intro screen
			FXMLLoader introLoader = new FXMLLoader(getClass().getResource("/FXML/ServerIntro.fxml"));
			introScene = new Scene(introLoader.load());
			introController = introLoader.getController();
			introController.setServerApp(this);
			
			// Load FXML game state screen
			FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("/FXML/ServerGameState.fxml"));
			gameStateScene = new Scene(gameLoader.load());
			gameStateController = gameLoader.getController();
			gameStateController.setServerApp(this);
			
			// Apply .css styles
			introScene.getStylesheets().add("/styles/ServerIntro.css");
			gameStateScene.getStylesheets().add("/styles/ServerGameState.css");
			
			// Set intro scene to pop up and display to user
			primaryStage.setScene(introScene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
	        System.exit(1);
		}
		
	}
	
	// Switches UI to Intro screen
	public void switchToIntroScreen() {
		introController.clearPortField();
		primaryStage.setScene(introScene);
	}
	
	// Switches UI to GameState screen
	public void switchToGameStateScreen() {
		primaryStage.setScene(gameStateScene);
	}
	
	// Getters
	public static ServerIntroController getIntroController() { return introController; }
	public static ServerGameStateController getGameStateController() { return gameStateController; }

}
