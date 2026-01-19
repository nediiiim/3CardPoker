import java.io.Serializable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

// Controller for Welcome.fxml
public class WelcomeController {
	@FXML
	private TextField nameField, ipField, portField;
	
	@FXML
	private Button connectButton;
	
	@FXML
	private Label titleLabel, statusLabel;
	
	private Client client;
	private MainClientApp mainApp;
	
	@FXML
	// Called when button is clicked, connects client to server and saves it info
	public void connectToServer() {
		String name = nameField.getText().trim();
		String ip = ipField.getText().trim();
		String portText = portField.getText().trim();
		
		// Validate inputs
		if (name.isEmpty()) {
			showAlert("Missing Information", "Please enter your name");
			return;
		}
		
		if (ip.isEmpty()) {
			showAlert("Missing Information", "Please enter an IP address");
			return;
		}
		
		if (portText.isEmpty()) {
			showAlert("Missing Information", "Please enter a port");
			return;
		}
		
		// Convert port text to integer and validate it's a real number
		int port = 0;
		try {
			port = Integer.parseInt(portText);
		} catch (Exception e) {
			showAlert("Invalid Port", "Port must be a valid number");
		}
		
		// Create and start client
		client = new Client(ip, port, data -> {
			Platform.runLater(() -> handleServerMessage(data));
		});
		
		client.start();
		
		statusLabel.setText("Connecting to server...");
		
		new Thread(() -> {
			try {
				Thread.sleep(1500);
				
				PokerInfo nameInfo = new PokerInfo("SET_NAME");
				nameInfo.setClientName(name);
				client.send(nameInfo);
				
				Platform.runLater(() -> {
					if (client.isConnected()) {
						statusLabel.setText("Connected");
						mainApp.switchToGameScreen();
					}
				});
			} catch (Exception e) {}
		}).start();
	}
	
	// Helper method to handle message from the server
	private void handleServerMessage(Serializable data) {
		String msg = data.toString();
		
		if ("CONNECT_FAIL".equals(msg)) {
			statusLabel.setText("Failed to connect");
			showAlert("Connection Error", "Unable to connect to server. Check IP and port. Server may be full.");
		}
	}
	
	// Helper function to display pop up
	private void showAlert(String title, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		
		// Load style sheet
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add("/styles/Welcome.css");
		dialogPane.getStyleClass().add("custom-alert");
		
		alert.showAndWait();
	}
	
	// Getters/Setters
	public void setMainApp(MainClientApp app) { this.mainApp = app; }
	public Client getClient() { return client; }
	
}
