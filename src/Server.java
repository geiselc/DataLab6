import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Server {
	/** Send/Receive Threads **/
	private Send s;
	private Receive r;
	
	private final int BUFFER_SIZE = 1024;	// max file size we can read at a time
	private int listenPort;					// server listening port
	private InetAddress clientIP;			// address of connected client
	private DatagramSocket serverSocket;	// server UDP Socket
	private String fileName;				// name of file to transfer

	public static void main(String[] args) {
		Server server = new Server(args[0]);
	}
	
	/**
	 * @param port = server listen port
	 */
	public Server(String port) {
		try{
			listenPort = Integer.parseInt(port);
			serverSocket = new DatagramSocket(listenPort);
			
			// Start threads:
			s = new Send();
			s.start();
			r = new Receive();
			r.start();
			while (true) {
				if (s.isAlive() || r.isAlive()) {

				} else {
					serverSocket.close();
					break;
				}
			}
		} catch (NumberFormatException e){
			System.out.println("Invalid port entered!");
			System.exit(1);
		} catch (SocketException e) {
			System.out.println("Unable to listen on this port!");
			System.exit(1);
		} 
	}
	
	private class Send extends Thread {
		public Send(){
			
		}
		
		public void run(){
			while(true){
				
			}
		}
	}

	private class Receive extends Thread {
		public Receive(){
			
		}
		
		public void run(){
			while(true){
				System.out.println("Listening ...");
			}
		}
	}
}
