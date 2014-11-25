package pack2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;

public class Server {
	private int portNumber;
	private Receive r;
	private Send s;
	private String fileName;
	private File file;
	private DatagramSocket serverSocket;
	private int clientPort;
	private InetAddress clientIP;
	private Path filePath;
	private int lastAck;
	private ArrayList<Integer> outstanding;

	public static void main(String[] args) {
		new  Server(args[0]);
	}
	public Server(String port) {
		try {
			lastAck = 0;
			System.out.println("The server ip is: "+InetAddress.getLocalHost());
			this.portNumber = Integer.parseInt(port);
			serverSocket = new DatagramSocket(portNumber);
			r = new Receive();
			r.start();

			while (true) {
				if (r.isAlive()) {
					
				} else {
					serverSocket.close();
					return;
				}
			}
		} catch (NumberFormatException e){
			System.out.println("Invalid port entered!");
			System.exit(1);
		} catch (SocketException e) {
			System.out.println("Unable to listen on this port!");
			System.exit(1);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
	}
	
	private class Receive extends Thread {
		
		public Receive() {
			
			if(getFirstConnection()) {
				if(processRequest()) {
					s = new Send();
					// TODO: set up so it has a sliding window of 5
					// based off of the last ack received
				}
			}
		}
		
		private boolean processRequest(){
			file = new File(fileName);
			
			if(file.exists()){
				filePath = Paths.get(file.getAbsolutePath());
				System.out.println("File found at: "+filePath);
				return true;
			}
			return false;
		}

		private boolean getFirstConnection() {
			
			// set up packet
			byte[] receiveData = new byte[1024];	
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			// Receive packet
			try {
				serverSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
			// get clientPort and clientIP
			clientPort = receivePacket.getPort();
			clientIP = receivePacket.getAddress();
			
			// get file name
			fileName = new String(receivePacket.getData(), 0, receivePacket.getLength());
			
			System.out.println("Received packer from " + clientIP+":"+clientPort+" asking for "+fileName);
			return true;
		}
		
		public boolean getPacketNumber() {
			return false;
		}
		public void getAcks() {
			
		}
	}
	
	private class Send extends Thread {
		
	}
}
