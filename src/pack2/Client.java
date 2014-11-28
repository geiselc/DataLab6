package pack2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class Client {
	private final int TIMEOUT = 1000;	
	private final int CLIENT_PORT = 9875;
	private int serverPort;				
	private InetAddress serverIP;			
	private DatagramSocket clientSocket;	
	private String fileName;				
	private InetAddress clientIP;
	private Send s;
	private Receive r;
	private HashMap<Integer, byte[]> data;
	private Header header;
	
	public static void main(String[] args) {
		new Client(args[0], args[1], args[2]);
	}
	
	public Client(String serverIP, String serverPort, String fileName) {
		try {
			int i = 1024;
			byte b = (byte) (i % 128);
			System.out.println(i + " " + b);
			System.exit(0);
			header = new Header();
			data = new HashMap<Integer, byte[]>();
			this.serverIP = InetAddress.getByName(serverIP);
			this.fileName = fileName;
			this.serverPort = Integer.parseInt(serverPort);
			clientSocket = new DatagramSocket(CLIENT_PORT);
			s = new Send();
			s.start();
			while(s.isAlive()) {
				
			}
			clientSocket.close();
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class Send extends Thread {
		public Send() {
			if(sendFileName()) {
				r = new Receive();
				r.start();
			}
		}
		public Send(int x) {
			ack(x);
		}
		private boolean sendFileName() {
			byte[] sendData = fileName.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIP, serverPort);
			try {
				clientSocket.send(sendPacket);
				System.out.println("Send packet to server asking for "+fileName);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		public void ack(int num) {
			String str = num+"";
			byte[] sendData = str.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIP, serverPort);
			try {
				clientSocket.send(sendPacket);
				System.out.println("Send packet to server asking for "+fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private class Receive extends Thread {
		public Receive() {
			receivePackets();
		}
		private void receivePackets() {
			while(true) {
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					clientSocket.receive(receivePacket);
					
					// TODO decode header on packet and update fields to be checked
					if(!header.fileExists()){
						System.out.println("File: "+fileName+" not found.");
						clientSocket.close();
						System.exit(0);
					}
					
					new Decode(receivePacket);
					
					// TODO code a method to check for sequence number, if accurate continue, else drop the packet 
					
					// Check to see if still data to receive from server
					if(header.isFin())
						break;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			new WriteFile();
		}
	}
	
	private class Decode extends Thread {
		public Decode(DatagramPacket packet) {
			System.out.println("HERE");
			byte[] packetData = packet.getData();
			Byte b = packetData[0];
			int index = b.intValue();
			index--; // Since incremented lastAck in server code
			new Send(index);
			System.out.println("Received packet #"+index);
			byte[] newData = new byte[packetData.length-1];
			for(int i = 0; i < newData.length; i++) {
				newData[i] = packetData[i+1];
			}
			data.put(index, newData);
		}
	}
	
	private class WriteFile extends Thread {
		public WriteFile() {
			System.out.println("Here");
			File file = new File(fileName);
			FileOutputStream fos = null;
			int tracker = 0;
			try {
				fos = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			for(int i = 0; i < data.size(); i++) {
				byte[] b = data.get(new Integer(i+1));
				try {
					fos.write(b, tracker, b.length);
				} catch (IOException e) {
					e.printStackTrace();
				}
				tracker+=b.length;
			}
			try {
				fos.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if(fos!=null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}