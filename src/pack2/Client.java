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
	private final int CLIENT_PORT = 9875;
	private int serverPort;				
	private InetAddress serverIP;			
	private DatagramSocket clientSocket;	
	private String fileName;				
	private Send s;
	private Receive r;
	private HashMap<Integer, byte[]> data;
	private Header header;
	private boolean written;
	private int last;
	
	public static void main(String[] args) {
		new Client(args[0], args[1], args[2]);
	}
	
	public Client(String serverIP, String serverPort, String fileName) {
		try {
			last = 999;
			written = false;
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
			
		}
		public void run() {
			if(sendFileName()) {
				r = new Receive();
				r.start();
				waiting();
			}
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
		public void waiting() {
			while(!written){
				
			}
		    return;
		}
	}
	
	private class Ack extends Thread {
		private int num;
		public Ack(int x) {
			num = x;
		}
		public void run() {
			String str = num+"";
			byte[] sendData = str.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIP, serverPort);
			try {
				clientSocket.send(sendPacket);
				System.out.println("Send packet to server acknowleding packet number "+num);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private class Receive extends Thread {
		public void run() {
			while(last+1 != data.size()) {
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					clientSocket.receive(receivePacket);
					byte[] headerData = new byte[6];
					for(int i = 0; i < headerData.length; i++) {
						headerData[i] = receivePacket.getData()[i];
					}
					header = new Header(headerData);
					
					if(!header.fileExists()){
						System.out.println("Received packet from server");
						System.out.println("File: "+fileName+" not found.");
						clientSocket.close();
						System.exit(0);
					}
					
					Decode d = new Decode(receivePacket, header);
					d.start();
					
					// Check to see if still data to receive from server
					if(header.isFin()) {
						last = header.getSeq();
					}
					if(last+1 == data.size())
						break;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class Decode extends Thread {
		private DatagramPacket packet;
		private Header h;
		public Decode(DatagramPacket p, Header h) {
			this.packet = p;
			this.h = h;
		}
		public void run() {
			byte[] packetData = packet.getData();
			System.out.println("Received packet #"+h.getSeq());
			byte[] newData = new byte[packetData.length-6];
			for(int i = 0; i < newData.length; i++) {
				newData[i] = packetData[i+6];
			}
			if (!data.containsKey(new Integer(h.getSeq()))) {
				data.put(new Integer(h.getSeq()), newData);
			}
			Ack ack = new Ack(h.getSeq());
			ack.start();
			
			if(last+1 == data.size()) {
				WriteFile wf = new WriteFile();
				wf.start();
			}
		}
	}
	
	private class WriteFile extends Thread {
		public void run() {
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
			written = true;
		}
	}
}