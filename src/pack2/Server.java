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
	private Header header;
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
	private boolean ack, rst, fin, fileExists;	// flags from header 
	private int seqNum;
	
	public static void main(String[] args) {
		new  Server(args[0]);
	}
	public Server(String port) {
		try {
			lastAck = seqNum = 0;
			header = new Header(ack, rst, fin, fileExists, seqNum, lastAck);
			outstanding = new ArrayList<Integer>();
			
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
			while(true){
				if(lastAck == 0) {
					if(getFirstConnection()){
						if(processRequest()) {
							s = new Send();
							s.start();
							//getAcks();
						} else {
							// Send packet with header field fileExists set false
							// Client will see this and exit. 
							
							header.setFileExists(false);
							// a little unsure of how to call send here
						}
					
					} else {
						// error establishing connection, retry
					}
				} else { 
					getAcks(); // I'm guessing this method will update our lastAck and seqNum, so we do that
					// Then call for a send here somehow
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
			
			header.setFileExists(false);
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
			
			lastAck++;
			System.out.println("Received packet from " + clientIP+":"+clientPort+" asking for "+fileName);
			return true;
		}
		
		public void getAcks() {
			// TODO
			// increase last ack if needed
			// remove from outstanding
			// have a list to store acks in case of packet out of order
		}
	}
	
	private class Send extends Thread {
		public Send() {
			while(true) {
				if(header.isFin()) {
					break;
				}
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return;
				}
				if(outstanding.size()<5) {
					int number = lastAck+1;
					while(true) {
						if(outstanding.contains(new Integer(number))) {
							number++;
						} else {
							break;
						}
					}
					
					byte[] data = new byte[1015];	// May need to update this once we attach header and know how big it is
					
					try {	
						if(fis.read(data) != -1){
							new SendPacket(data, number);
							outstanding.add(new Integer(number));
								try {
									fis.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
						} else {
							System.out.println("Reached end of file");
							header.setFin(true);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} 
			}
		}
	}
	
	private class SendPacket extends Thread {
		public SendPacket(byte[] data, int index) {
			byte b = (byte)index;
			byte[] sendData = new byte[data.length+1];
			sendData[0] = b;
			
			/**
			 * TODO
			 * Attach header to sendData before sending it
			 */
			
			for(int i = 1; i < sendData.length; i++) {
				sendData[i] = data[i-1];
			}
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIP, clientPort);
			try {
				serverSocket.send(sendPacket);
				System.out.println("Send packet number " + index +" to client");
			} catch (IOException e) {
				outstanding.remove(new Integer(index));
				// i am thinking that a timeout on the send or
				// another error will occur here, so we will just
				// say that we are no longer trying to send the packet so
				// we can re send it above
				e.printStackTrace();
			}
		}
	}
}
