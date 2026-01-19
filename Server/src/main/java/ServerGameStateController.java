import java.io.Serializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

// Controller for ServerGameState.fxml
public class ServerGameStateController implements Initializable{
	@FXML
	private Label statusLabel, clientCountLabel, highestWinnerLabel, highestLoserLabel;
	
	@FXML
	private ListView<String> serverLog;
	
	@FXML
	private Button stopButton;
	
	private Server server;
	private ServerApp serverApp;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}
	
	// Helper method to handle messages coming from the server
	private void handleServerUpdate(Serializable data) {
		String message = data.toString();
		
		// Server either notifies to update stats or add to server log
		if ("UPDATE_STATS".equals(message)) {
			updateStatsDisplay();
			return;
		}
		
		appendToServerLog(message);
	}
	
	// Helper method to refresh number of clients, highest winner, highest loser
	private void updateStatsDisplay() {
		clientCountLabel.setText(Integer.toString(server.getNumClients()));
		
		String winner = server.getHighestWinnerName();
		int winAmount = server.getHighestWinnerAmount();
		
		if (winner == null || winner.equals("N/A") || winAmount == 0) {
			highestWinnerLabel.setText("N/A");
		} else {
			if (winAmount >= 0) {
				highestWinnerLabel.setText(winner + " ($" + winAmount + ")");
			} else {
				highestWinnerLabel.setText(winner + " (-$" + Math.abs(winAmount) + ")");
			}
		}
		
		String loser = server.getHighestLoserName();
		int loseAmount = server.getHighestLoserAmount();
		
		if (loser == null || loser.equals("N/A") || loseAmount == 0) {
			highestLoserLabel.setText("N/A");
		} else {
			if (loseAmount >= 0) {
				highestLoserLabel.setText(loser + " ($" + loseAmount + ")");
			} else {
				highestLoserLabel.setText(loser + " (-$" + Math.abs(loseAmount) + ")");
			}
		}
	}
	
	// Stop server button functionality
	public void turnOffServer() {
		if (server != null) {
			server.stopServer();
		}
		serverApp.switchToIntroScreen();
	}
	
	// Add message to data log
	public void appendToServerLog(String message) {
		Platform.runLater(() -> {
			serverLog.getItems().add(message);
			serverLog.scrollTo(serverLog.getItems().size() - 1);
		});
	}
	
	// Clears server log
	public void clearServerLog() {
		Platform.runLater(() -> serverLog.getItems().clear());
	}
	
	
	// Getters/Setters
	public void setServerApp(ServerApp serverApp) { this.serverApp = serverApp; }
	public void setServer(Server server) {
		this.server = server;
		
		// Give server callback to send updates back to this GUI
		server.setCallback(data -> {
			Platform.runLater(() -> handleServerUpdate(data));
		});
		
		updateStatsDisplay();
	}
}
