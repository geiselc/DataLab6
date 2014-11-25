import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
			
			//TODO Finish Three-Way Handshake
			/** Three-Way Handshake **/
			sendSynPacket();
			receiveSynAckPacket();
			sendAckPacket();
			
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
		//Files f;
		Path p;
		p = Paths.get(System.getProperty("user.dir")); 
		System.out.println(p);
		try {
			//String s = new String(data, "UTF-8");
			
			//File file = new File(s);
			if(Files.isWritable(p)){
				FileOutputStream fo = new FileOutputStream(""+p.toString()+"\\"+fileName);
				fo.write(data);
				//fo.write(b, off, len);
				//Files.write(p, data, StandardOpenOption.CREATE);
				fo.close();
				
				//FileInputStream fi =  new FileInputStream(""+p.toString()+fileName);
			} else
				System.out.println("Cannot write to this directory");
			
		} catch (IOException e) {
			System.out.println("Could not write file");
			e.printStackTrace();
		}
	}
	
	public void sendSynPacket(){
		byte[] synData = new byte[BUFFER_SIZE]; //TODO What do we want in the SYN packet?
		sendPacket = new DatagramPacket(synData, synData.length, serverIP, serverPort);
		try {
			clientSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("Failed to send SYN packet!");
			e.printStackTrace();
		}
		return;
	}
	
	public void receiveSynAckPacket(){
		byte[] receiveData = new byte[BUFFER_SIZE];
		
		receivePacket = new DatagramPacket(receiveData, receiveData.length, clientIP, clientPort);
		try {
			clientSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.println("Couldn't get SYN-ACK packet from server!");
			e.printStackTrace();
		}
		receiveData = receivePacket.getData();
		
		//TODO When we get the SYN-ACK what do we want to do with it?
		
		return;
	}
	
	public void sendAckPacket(){
		byte[] synData = new byte[BUFFER_SIZE]; //TODO What do we want in the ACK packet?
		sendPacket = new DatagramPacket(synData, synData.length, serverIP, serverPort);
		try {
			clientSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("Failed to send ACK packet!");
			e.printStackTrace();
		}
		return;
	}
}
