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
	private static boolean written;
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
			while (s.isAlive()) {
				System.out.println(Thread.activeCount());
			}
			clientSocket.close();
			// System.exit(0);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class Send extends Thread {
		public Send() {

		}

		public void run() {
			if (sendFileName()) {
				r = new Receive();
				r.start();
				waiting();
				}
		}

		private boolean sendFileName() {

			Header h = new Header();
			h.generateChecksum(fileName.getBytes());
			byte[] fileBytes = fileName.getBytes();
			byte[] sendData = new byte[2 + fileBytes.length];
			String cSum = Header.genCheckSumStatic(fileBytes);
			String str1 = cSum.substring(0, 7);
			sendData[0] = (byte) Integer.parseInt(str1, 2);
			String str2 = cSum.substring(8);
			sendData[1] = (byte) Integer.parseInt(str2, 2);
			
			for(int i = 2; i < fileBytes.length; i++){
				sendData[i] = fileBytes[i-2];
			}
			
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, serverIP, serverPort);
			
			try {
				clientSocket.send(sendPacket);
				System.out.println("Send packet to server asking for "
						+ fileName);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		public void waiting() {
			while (true) {
				if (Client.written)
					break;
				else
					System.out.println("");
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
			byte[] ackData = new byte[1];
			ackData[0] = (byte) num;
			
			String cSum = Header.genCheckSumStatic(ackData);
			
			byte[] sendData = new byte[3];
			sendData[2] = (byte) num;
			
			String str1 = cSum.substring(0, 7);
			sendData[0] = (byte) Integer.parseInt(str1, 2);
			String str2 = cSum.substring(8);
			sendData[1] = (byte) Integer.parseInt(str2, 2);
			
			
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, serverIP, serverPort);
			try {
				clientSocket.send(sendPacket);
				System.out
						.println("Send packet to server acknowleding packet number "
								+ num);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class Receive extends Thread {
		public void run() {
			while (last != data.size()) {
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				try {
					clientSocket.receive(receivePacket);
					Header h = new Header();
					
					if(noError(receivePacket.getData(), h)){
						if (!header.fileExists()) {
							System.out.println("Received packet from server");
							System.out.println("File: " + fileName
									+ " not found.");
							Ack a = new Ack(0);
							a.start();
							while (a.isAlive()) {

							}
							clientSocket.close();
							System.exit(0);
						}

						Decode d = new Decode(receivePacket, header);
						d.start();

						// Check to see if still data to receive from server
						if (header.isFin()) {
							last = header.getSeq();
						}
						// System.out.println(last);
						// System.out.println(data.size());
						// System.out.println(header.getSeq());
						// System.out.println(header.isFin());
						if (last == data.size() + 1) {
							System.out.println("Breaking");
							break;
						}
					} else {
						System.out.println("Checksum Mismatch");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private boolean noError(byte[] input, Header h){
			byte[] nonHead = new byte[1024 - h.SIZE];
			for(int i = 0; i < nonHead.length; i++){
				nonHead[i] = input[i + h.SIZE];
			}
			
			byte[] headData = new byte[h.SIZE];
			for(int i = 0; i < headData.length; i++){
				headData[i] = input[i];
			}
			
			h = new Header(headData);
			String cSum = h.getCheckSum();
			h.generateChecksum(nonHead);
			String cSum2 = h.getCheckSum();
			
			
			if(trimAndCheck(cSum, cSum2))
				return true;
			
			return false;
		}
		
		private boolean trimAndCheck(String one, String two) {
			int index = one.indexOf("1");
			one = one.substring(index);
			
			index = two.indexOf("1");
			two = two.substring(index);
			
			return one.equals(two);
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
			System.out.println("Received packet #" + h.getSeq());
			byte[] newData = new byte[packetData.length - 10];
			for (int i = 0; i < newData.length; i++) {
				newData[i] = packetData[i + 10];
			}
			if (!data.containsKey(new Integer(h.getSeq()))) {
				data.put(new Integer(h.getSeq()), newData);
			}
			Ack ack = new Ack(h.getSeq());
			ack.start();

			if (last == data.size()) {
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

			for (int i = 0; i < data.size(); i++) {
				byte[] b = data.get(new Integer(i + 1));
				try {
					fos.write(b, tracker, b.length);
				} catch (IOException e) {
					e.printStackTrace();
				}
				tracker += b.length;
			}
			try {
				fos.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Client.written = true;
		}
	}
}
