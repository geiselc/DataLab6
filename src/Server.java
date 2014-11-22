import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
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
			// Establish ports, open sockets, and start listening ...
			listenPort = Integer.parseInt(port);
			serverSocket = new DatagramSocket(listenPort);
			
			// Start threads:
			s = new Send();
			s.start();
			r = new Receive();
			r.start();
			while (true) {
				if (s.isAlive() || r.isAlive()) {
					// Run while open
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
	
	/** Send file/packet back to client **/
	private class Send extends Thread {
		public Send(){
			
		}
		
		public void run(){
			while(true){
				
			}
		}
	}

	/** Receive request from Client **/
	private class Receive extends Thread {
		DatagramPacket receivePacket;
		
		public Receive(){
			
		}
		
		public void run(){
			while(true){				
				System.out.println("Listening ...");
				/* Establish connection to client */
		
				/* Get Request for file from client*/
				byte[] receiveData = new byte[BUFFER_SIZE];	// max of 1024
				
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					serverSocket.receive(receivePacket);
				} catch (IOException e) {
					System.out.println("Failed to get packet!");
					e.printStackTrace();
				}
				System.out.println("Accepted connection");
				System.out.println("Recieved: "+new String(receivePacket.getData(), 0, receivePacket.getLength()));
				
			}
		}
	}
}
