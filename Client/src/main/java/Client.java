import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

// Handles all communication between client and server
public class Client extends Thread{
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Consumer<Serializable> callback;
	private String ip;
	private int port;
	private boolean connected = false;
	

	// Constructor that assigns callback, ip, and port
	public Client(String ip, int port, Consumer<Serializable> call) {
		callback = call;
		this.ip = ip;
		this.port = port;
	}
	
	// Attempts to connect to Server and listens for messages
	@Override
	public void run() {
		try {
			socket = new Socket(ip, port);
			
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			socket.setTcpNoDelay(true);
			
			connected = true;
		} catch (Exception e) {
			callback.accept("CONNECT_FAIL");
			return;
		}
		
		// Listen for server messages
		while (true) {
			Serializable data = null;
				
			try {
				data = (Serializable) in.readObject();
			} catch (Exception e) {
				callback.accept("SERVER_SHUTDOWN");
				break;
			}
			
			if ("SERVER_SHUTDOWN".equals(data)) {
				callback.accept("SERVER_SHUTDOWN");
				break;
			}
				
			callback.accept(data);
		}
		try { socket.close(); } catch (Exception e) {}
	}
	
	// Sends a Serializable object to the server
	public void send(Serializable data) {
		try {
			out.writeObject(data);
		} catch (Exception e) {
			callback.accept("Error sending data to server");
		}
	}
	
	// Closes client connection
	public void closeConnection() {
		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (Exception e) {
			callback.accept("Error closing connection");
		}
	}
	
	// Getters/Setters
	public void setCallback(Consumer<Serializable> call) { this.callback = call; }
	public boolean isConnected() { return connected; }
}
