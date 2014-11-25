//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//
//public class Server {
//	/** Send/Receive Threads **/
//	private Send s;
//	private Receive r;
//	
//	private final int BUFFER_SIZE = 1016;	// max file size we can read at a time
//	private int listenPort;					// server listening port
//	private InetAddress clientIP;			// address of connected client
//	private int clientPort;					// port the client should be listening on
//	private DatagramSocket serverSocket;	// server UDP Socket
//	private String fileName;				// name of file to transfer
//	private byte[] receiveData;				// data received
//	private byte[] sendData;				// data to send
//
//	public static void main(String[] args) {
//		new Server(args[0]);
//	}
//	
//	/**
//	 * @param port = server listen port
//	 */
//	public Server(String port) {
//		try{
//			System.out.println("The server ip is: "+InetAddress.getLocalHost());
//		
//			// Establish ports, open sockets, and start listening ...
//			listenPort = Integer.parseInt(port);
//			serverSocket = new DatagramSocket(listenPort);
//			
//			/** Start receiving thread:
//			 * 	Not calling the send thread here, because we only send after we've
//			 *  received and processed a request. Therefore the server should only
//			 *  sit idle with a receive thread open to listen for file requests
//			 *  from clients. 
//			 */
//			r = new Receive();
//			r.start();
//
//			while (true) {
//				if (r.isAlive()) {
//					// Run while open
//				} else {
//					serverSocket.close();
//					break;
//				}
//			}
//		} catch (NumberFormatException e){
//			System.out.println("Invalid port entered!");
//			System.exit(1);
//		} catch (SocketException e) {
//			System.out.println("Unable to listen on this port!");
//			System.exit(1);
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		} 
//	}
//	
//	/** Send file/packet back to client **/
//	private class Send extends Thread {
//		DatagramPacket sendPacket;
//		
//		public Send(){
//			
//		}
//		
//		public void run(){
//			while(true){
//				// If we have a file to send, send it
//				if(!(fileName == null)){
//					System.out.println("Sending ...");
//					sendData = new byte[BUFFER_SIZE];
//					File f = new File("fileName");
//					try {
//						FileInputStream fis = new FileInputStream(f);
//						fis.read(sendData, 0, BUFFER_SIZE);
//						fis.close();
//					} catch (FileNotFoundException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					sendData = fileName.getBytes();
//					sendPacket = new DatagramPacket(sendData, sendData.length, clientIP, clientPort);
//					try {
//						serverSocket.send(sendPacket);
//					} catch (IOException e) {
//						System.out.println("Cannet send packet!");
//						e.printStackTrace();
//					}	
//					System.out.println("Sent to client: "+sendPacket.getData().toString().getBytes());
//					return;
//				} else
//					continue; // wait until we have a file to send
//			}
//		}
//	}
//
//	/** Receive request from Client **/
//	private class Receive extends Thread {
//		DatagramPacket receivePacket;
//		File file;
//		Path filePath;
//		
//		public Receive(){
//			
//		}
//		
//		public void run(){
//			while(true){				
//				System.out.println("Listening ...");
//				/* Establish connection to client */
//		
//				/* Get Request for file from client*/
//				receiveData = new byte[BUFFER_SIZE];	// max of 1024
//				
//				receivePacket = new DatagramPacket(receiveData, receiveData.length);
//				
//				System.out.println("Client Connected: "+clientIP+" on port: "+clientPort);
//				try {
//					serverSocket.receive(receivePacket);
//					clientIP = receivePacket.getAddress();
//					clientPort = receivePacket.getPort();
//				} catch (IOException e) {
//					System.out.println("Failed to get packet!");
//					e.printStackTrace();
//				}
//				System.out.println("Accepted connection");
//				fileName = new String(receivePacket.getData(), 0, receivePacket.getLength());
//				System.out.println("Recieved: "+fileName);
//				if(processRequest()){
//					System.out.println("File found. Now to send");
//					try {
//						byte[] sendData = new byte[BUFFER_SIZE];
//						sendData = Files.readAllBytes(filePath);
//					} catch (IOException e) {
//						System.out.println("Couldn't get file into bytes");
//						e.printStackTrace();
//					}
//				} else {
//					System.out.println("File not found.");
//				}
//				
//				s = new Send();
//				s.start();
//			}
//		}
//		
//		public boolean processRequest(){
//			file = new File(fileName);
//			
//			if(file.exists()){
//				filePath = Paths.get(file.getAbsolutePath());
//				System.out.println("File found at: "+filePath);
//				return true;
//			} else
//				return false;
//		}
//	}
//}
