import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

	/** Constants **/
	private final int BUFFER_SIZE = 1024;	// max file size we can read at a time
	private final int TIMEOUT = 1000;		// arbitrary, we can go with however long we want. I'm dumping 1000 here for no reason. 
	
	private int serverPort;				
	private InetAddress serverIP;			// address of server
	private DatagramSocket clientSocket;	// client UDP Socket
	private String fileName;				// name of file to transfer
	private InetAddress clientIP;
	private int clientPort = 9875;
	
	DatagramPacket sendPacket;				// packet to send to server
	DatagramPacket receivePacket;			// packet got from server

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
			clientSocket = new DatagramSocket(clientPort);
			//clientSocket.setTimeout(TIMEOUT);
			
			serverIP = InetAddress.getByName(ip);
			fileName = file;
			
			/** Ask server for file **/
			sendFileRequest();
			
			/** Get file from server **/
			getRequestedFile();
			
			/** Once file is received, write it locally **/
			writeFile();
			
			/** Close the socket when all is said and done **/
			clientSocket.close();
		
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
	
	public void sendFileRequest(){
	
		byte[] sendData = new byte[BUFFER_SIZE];	// max of 1024
			
		/* TODO Get file request and header info into buffer byte array */
		sendData = fileName.getBytes();	// Not sure if we need to include exact file path or just filename
		
		sendPacket = new DatagramPacket(sendData, sendData.length, serverIP, serverPort);
		try {
			clientSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("Failed to send packet!");
			e.printStackTrace();
		}
	}
	
	public void getRequestedFile(){
		byte[] receiveData = new byte[BUFFER_SIZE];
		
		receivePacket = new DatagramPacket(receiveData, receiveData.length, clientIP, clientPort);
		try {
			clientSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.println("Couldn't get packet from server!");
			e.printStackTrace();
		}
		receiveData = receivePacket.getData();
		System.out.println("Client Received: "+receiveData.toString().getBytes());
	}
	
	public void writeFile(){
		byte[] data = receivePacket.getData();
		String s = data.toString();
		File file = new File(receivePacket.getData().toString());
		try {
			file.createNewFile();
		} catch (IOException e) {
			System.out.println("Could not write file");
			e.printStackTrace();
		}
	}
}
