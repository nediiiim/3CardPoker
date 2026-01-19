import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

// This class is responsible for starting server socket, managing 8 client connections, relaying updates to GUI, and storing client threads
public class Server {
	private int clientCount = 0, port;
	private int highestWinnerAmount, highestLoserAmount = 0;
	private String highestWinnerName, highestLoserName = "";
	private ArrayList<ClientThread> clients = new ArrayList<>();
	private TheServer server;
	private Consumer<Serializable> callback;
	private HashMap<String, Integer> currentTotals = new HashMap<>();
	
	// Constructor with Consumer parameter
	// Starts server thread
	public Server(Consumer<Serializable> call) {
		callback = call;
		server = new TheServer();
		server.start();
	}
	
	// Stops the server and closes all connected client games
	public void stopServer() {
		try {
			Consumer<Serializable> oldCallback = callback;
			callback = msg -> {};
			
			oldCallback.accept("SERVER_SHUTDOWN");
			
			ArrayList<ClientThread> safeCopy = new ArrayList<>(clients);
			
			// Shut down each client
			for (ClientThread client : safeCopy) {
				try {
					client.send(new PokerInfo("SERVER_SHUTDOWN"));
				} catch (Exception e) {}
				
				try { client.in.close(); } catch(Exception e) {}
				try { client.out.close(); } catch (Exception e) {}
				try { client.connection.close(); } catch (Exception e) {}
			}
			clients.clear();
			clientCount = 0;
			
			// Restore callback
			callback = oldCallback;
			callback.accept("Server stopped.\n");
			
		} catch (Exception e) {
			callback.accept("Error stopping server.\n");
		}
	}
	
	// Notifies GUI that something changed (scores, client count, etc.)
	public void broadcastStatsUpdate() {
		callback.accept("UPDATE_STATS");
	}
	
	// Recomputes the highest winner and highest loser stats
	public void recomputeHighScores() {
		highestWinnerAmount = -999999999;
		highestLoserAmount = 999999999;
		highestLoserName = "N/A";
		highestWinnerName = "N/A";
		
		for (var entry : currentTotals.entrySet()) {
			String name = entry.getKey();
			int amount = entry.getValue();
			
			if (amount > highestWinnerAmount) {
				highestWinnerAmount = amount;
				highestWinnerName = name;
			}
			if (amount < highestLoserAmount) {
				highestLoserAmount = amount;
				highestLoserName = name;
			}
		}
	}
	
	// Ensures no two players have the same name
	// If exists then adds (2) or (3), etc. to the name
	private String makeUniqueName(String baseName) {
		String name = baseName;
		
		// If name not taken, return as normal
		if (!currentTotals.containsKey(name) && clients.stream().noneMatch(c -> c.clientName.equals(name))) {
			return name;
		}
		
		// Otherwise append (2), (3), etc..
		int suffix = 2;
		while (true) {
			String attempt = baseName + "(" + suffix + ")";
			boolean exists = currentTotals.containsKey(attempt) || clients.stream().anyMatch(c -> c.clientName.equals(attempt));
			
			if (!exists) {
				return attempt;
			}
			suffix++;
		}
	}
	
	// Listens for new connections
	class TheServer extends Thread {
		public void run() {
			// Creates server socket
			try (ServerSocket mySocket = new ServerSocket(port);){

				callback.accept("Server started on port " + port);
				
				// Listens for client connections
				while (true) {
					Socket clientSocket = mySocket.accept();
					
					// Checks if server reached max clients (8)
					if (clientCount >= 8) {
						callback.accept("Connection attempt rejected: Server Full\n");
						clientSocket.close();
						continue;
					}
					
					// Accept client
					clientCount++;
					ClientThread c = new ClientThread(clientSocket, clientCount);
					clients.add(c);
					c.start();
					
					broadcastStatsUpdate();
				}
			}
			catch (Exception e) {
				callback.accept("Server Socket failed to launch on port " + port + ". Please close the app and relaunch if you want to use the same port again.");
			}
		}
	}
	
	// Handles communication with a connected client
	class ClientThread extends Thread {
		private Socket connection;
		private String clientName;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		private PokerGame pokerGame;
		
		// Constructor that takes Socket and Client ID as parameters
		ClientThread(Socket s, int clientID) {
			this.connection = s;
			this.clientName = "Client # " + clientID;
			this.pokerGame = new PokerGame();
		}
		
		// Sends PokerInfo object to the client
		public void send(PokerInfo info) {
			try {
				out.writeObject(info);
			} catch (Exception e) {
				callback.accept("Error sending to " + clientName + "\n");
			}
		}
		
		// Reads PokerInfo object and handles messages
		public void run() {
			try {
				// Set up streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				connection.setTcpNoDelay(true);	
				
				while (true) {
					// Read object from client
					PokerInfo info = (PokerInfo) in.readObject();
					
					String type = info.getMessageType();
					
					// Checks if client input a name
					if ("SET_NAME".equals(type) && info.getClientName() != null) {
						String base = info.getClientName();
						clientName = makeUniqueName(base);
						
						callback.accept(clientName + " has connected! Total clients: " + clientCount + "\n");
						
						currentTotals.putIfAbsent(clientName, 0);
						
						broadcastStatsUpdate();
						continue;
					}
					
					// Handles start game request
					if ("START_GAME".equals(type)) {
						callback.accept(clientName + " requested a new game\n");
						
						pokerGame.setAnteBet(info.getAnteBet());
						pokerGame.setPairPlusBet(info.getPairPlusBet());
						
						PokerInfo response = pokerGame.startNewRound();
						
						send(response);
						continue;
					}
					
					// Handles play request
					if ("PLAY".equals(type)) {
						callback.accept(clientName + " chose to play\n");
						
						pokerGame.setPlayBet(pokerGame.getAnteBet());
						
						PokerInfo result = pokerGame.playRound();
						send(result);
						
						currentTotals.put(clientName, (int) pokerGame.getTotalWinnings());
						
						callback.accept(clientName + " result:\n" + result.getResultMessage() + "\n");
						
						recomputeHighScores();
						broadcastStatsUpdate();
						continue;
					}	
					
					// Handles fold request
					if ("FOLD".equals(type)) {
						callback.accept(clientName + " chose to fold\n");
						
						pokerGame.setAnteBet(info.getAnteBet());
						pokerGame.setPairPlusBet(info.getPairPlusBet());
						PokerInfo result = pokerGame.foldRound();
						send(result);
						
						currentTotals.put(clientName, (int) pokerGame.getTotalWinnings());
						callback.accept(clientName + " result:\n" + result.getResultMessage() + "\n");
						
						recomputeHighScores();
						broadcastStatsUpdate();
						continue;
					}
					
					// Handles quit request
					if ("QUIT".equals(info.getMessageType())) {
						callback.accept(clientName + " has quit the game. \n");
						break;
					}
					
					// Handles reset game request
					if ("RESET_GAME".equals(type)) {
						callback.accept(clientName + " reset their game\n");
						
						PokerInfo result = pokerGame.resetGame();
						send(result);
						
						currentTotals.put(clientName, (int) pokerGame.getTotalWinnings());
						recomputeHighScores();
						broadcastStatsUpdate();
						continue;
					}
					
					callback.accept(clientName + " sent: " + info.getMessageType() + "\n");
				}
			}
			catch(Exception e) {
				// Disconnect client
				callback.accept(clientName + " disconnected.\n");
				clients.remove(this);
				clientCount--;
				
				currentTotals.remove(clientName);
				recomputeHighScores();
				broadcastStatsUpdate();
				
				callback.accept("Total clients now: " + clientCount + "\n");
				try { 
					connection.close();
				} 
				catch(Exception f) {}
			}
		}
	}
	
	// Setters/Getters
	public void setPort(int port) { this.port = port; }
	public String getHighestWinnerName() { return highestWinnerName; }
	public String getHighestLoserName() { return highestLoserName; }
	public int getHighestWinnerAmount() { return highestWinnerAmount; }
	public int getHighestLoserAmount() { return highestLoserAmount; }
	public int getNumClients() { return clientCount; }
	public void setCallback(Consumer<Serializable> call) { this.callback = call; }
}
