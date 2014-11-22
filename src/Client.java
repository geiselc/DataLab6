import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

	/** Send/Receive Threads **/
	private Send s;
	private Receive r;
	
	/** Constants **/
	private final int BUFFER_SIZE = 1024;	// max file size we can read at a time
	private final int TIMEOUT = 1000;		// arbitrary, we can go with however long we want. I'm dumping 1000 here for no reason. 
	
	private int serverPort;				
	private InetAddress serverIP;			// address of server
	private DatagramSocket clientSocket;	// client UDP Socket
	private String fileName;				// name of file to transfer

	public static void main(String[] args){
		Client client = new Client(args[0], args[1], args[2]);
	}
	
	/**
	 * 
	 * @param port = server port to connect to
	 * @param ip = server ip address
	 * @param file = file to request
	 */
	public Client(String port, String ip, String file) {
		try{
			serverPort = Integer.parseInt(port);
			clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(TIMEOUT);
			
			serverIP = InetAddress.getByName(ip);
			fileName = file;
			
			// Start threads:
			s = new Send();
			s.start();
			r = new Receive();
			r.start();
			while (true) {
				if (s.isAlive() || r.isAlive()) {
				
				} else {
					clientSocket.close();
					break;
				}
			}
		} catch (NumberFormatException e){
			System.out.println("Invalid port entered!");
			e.printStackTrace();
			System.exit(1);
		} catch (SocketException e) {
			System.out.println("Unable to connect to server on this port!");
			e.printStackTrace();
			System.exit(1);
		} catch (UnknownHostException e) {
			System.out.println("Couldn't connect to server address!");
			e.printStackTrace();
			System.exit(1);
		} 
	}
	
	private class Send extends Thread {
		DatagramPacket sendPacket;
		public Send(){
			
		}
		
		public void run(){
			while(true){
				
				byte[] sendData = new byte[BUFFER_SIZE];	// max of 1024
				
				sendData = fileName.getBytes();	// Not srue if we need to include exact file path or just filename
				
				sendPacket = new DatagramPacket(sendData, sendData.length, serverIP, serverPort);
				try {
					clientSocket.send(sendPacket);
				} catch (IOException e) {
					System.out.println("Failed to send packet!");
					e.printStackTrace();
				}
			}
		}
	}

	private class Receive extends Thread {
		public Receive(){
			
		}
		
		public void run(){
			while(true){
				
			}
		}
	}
}
