import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

	/** Send/Receive Threads **/
	private Send s;
	private Receive r;
	
	private int serverPort;					// server listening port
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
			clientSocket = new DatagramSocket(serverPort);
		
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
				
			}
		}
	}
}
