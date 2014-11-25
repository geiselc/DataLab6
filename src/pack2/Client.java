package pack2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
	private final int BUFFER_SIZE = 1024;
	private final int TIMEOUT = 1000;	
	private final int CLIENT_PORT = 9875;
	private int serverPort;				
	private InetAddress serverIP;			
	private DatagramSocket clientSocket;	
	private String fileName;				
	private InetAddress clientIP;
	private Send s;
	private Receive r;
	
	public static void main(String[] args) {
		new Client(args[0], args[1], args[2]);
	}
	
	public Client(String serverIP, String serverPort, String fileName) {
		try {
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
				getAndSendPackets();
			}
		}
		private boolean sendFileName() {
			byte[] sendData = fileName.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIP, serverPort);
			try {
				clientSocket.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		private void getAndSendPackets() {
			
		}
	}
	private class Receive extends Thread {
		
	}
}
