package pack2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

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
	private HashMap<Integer, byte[]> data;
	
	public static void main(String[] args) {
		new Client(args[0], args[1], args[2]);
	}
	
	public Client(String serverIP, String serverPort, String fileName) {
		try {
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
					new Decode(receivePacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class Decode extends Thread {
		public Decode(DatagramPacket packet) {
			byte[] packetData = packet.getData();
			Byte b = packetData[0];
			int index = b.intValue();
			new Send(index);
			System.out.println("Received packet #"+index);
			byte[] newData = new byte[packetData.length-1];
			for(int i = 0; i < newData.length; i++) {
				newData[i] = packetData[i+1];
			}
			data.put(index, newData);
		}
	}
}
