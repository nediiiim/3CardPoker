import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

// Controller for ServerIntro.fxml
public class ServerIntroController implements Initializable{
	@FXML
	private TextField portTextField; // Where user inputs port number
	
	@FXML
	private Button startButton; // Starts server
	
	@FXML
	private Label statusLabel; // Display if server is on or off
	
	private Server server; // Server instance
	private ServerApp serverApp;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}
	
	// Start button functionality
	// Validates port number, starts the server, and switches to game state screen
	@FXML
	public void startButtonClicked() {
		String portText = portTextField.getText().trim();
		
		// Check if port field is empty
		if (portText.isEmpty()) {
			displayAlert("Port Required", "Please enter a port number.");
			return;
		}
		
		int port;
		try {
			port = Integer.parseInt(portText);
		} catch (NumberFormatException e) {
			displayAlert("Invalid Port", "Port must be a number.");
			return;
		}
		
		ServerGameStateController gameController = ServerApp.getGameStateController();
		gameController.clearServerLog();
		
		// Start server and set port
		server = new Server(data -> {});
		server.setPort(port);
		
		gameController.setServer(server);
		
		// Switch to game state screen
		serverApp.switchToGameStateScreen();
	}
	
	// Helper function to display an alert(error message)
	private void displayAlert(String title, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		
		// Sets alert text fields
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		
		// Load style sheet
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add("/styles/ServerIntro.css");
		dialogPane.getStyleClass().add("custom-alert");
		
		alert.showAndWait();
	}
	
	// Helper function to clear text field for port
	public void clearPortField() {
		portTextField.clear();
	}
	
	// Getters/Setters
	public void setServerApp(ServerApp serverApp) { this.serverApp = serverApp; }
	public Server getServer() { return server; }
}
