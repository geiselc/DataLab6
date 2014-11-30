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
import java.util.Collections;
import java.util.HashMap;

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
	private ArrayList<Integer> gotAcks;
	private boolean lastAckReceived;
	private int lastAckNumber;
	private HashMap<Integer, byte[]> map;

	public static void main(String[] args) {
		new Server(args[0]);
	}

	public Server(String port) {
		try {
			lastAck = -1;
			outstanding = new ArrayList<Integer>();
			gotAcks = new ArrayList<Integer>();
			lastAckReceived = false;
			lastAckNumber = 999;
			map = new HashMap<Integer, byte[]>();

			System.out.println("The server ip is: "
					+ InetAddress.getLocalHost());
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
		} catch (NumberFormatException e) {
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

		public void run() {
			while (true) {
				if (getFirstConnection()) {
					if (processRequest()) {
						s = new Send();
						s.start();
						gotAcksLoop();
					} else {
						NoFilePacket nfp = new NoFilePacket();
						nfp.start();
						lastAckNumber = 0;
						gotAcksLoop();
					}

				} else {
					// error establishing connection, retry
				}
			}
		}

		private boolean processRequest() {
			file = new File(fileName);

			if (file.exists()) {
				filePath = Paths.get(file.getAbsolutePath());
				System.out.println("File found at: " + filePath);
				return true;
			}

			header.setFileExists(false);
			return false;
		}

		private boolean getFirstConnection() {
			// set up packet
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);

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
			fileName = new String(receivePacket.getData(), 0,
					receivePacket.getLength());

			lastAck++;
			System.out.println("Received packet from " + clientIP + ":"
					+ clientPort + " asking for " + fileName);
			return true;
		}

		public void gotAcksLoop() {

			while (!lastAckReceived) {
				if (outstanding.size() > 0) {
					byte[] receiveData = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);

					// Receive packet
					try {
						serverSocket.receive(receivePacket);
						byte b = receivePacket.getData()[0];
						int seqNum = (int) b;
						getAcks(seqNum);
					} catch (Exception e) {
						// timeout has occured
						// so we do not ack their ack,
						// meaning the packet will be sent again
					}
				}
			}
		}

		public void getAcks(int seq) {
			if (!gotAcks.contains(new Integer(seq))) {
				gotAcks.add(new Integer(seq));
			}
			if (outstanding.contains(new Integer(seq))) {
				outstanding.remove(new Integer(seq));
			}
			// increase last ack if needed
			if (seq == lastAck + 1) {
				lastAck++;
				Collections.sort(gotAcks);
				for (int i = seq + 1; i < gotAcks.size(); i++) {
					if (gotAcks.get(i) == lastAck + 1) {
						lastAck++;
					} else {
						break;
					}
				}
			}
			if (lastAck == lastAckNumber) {
				lastAckReceived = true;
			}
		}
	}

	private class Send extends Thread {
		public void run() {
			while (r.isAlive()) {
				FileInputStream fis = null;
				try {
					if (fis == null) {
						fis = new FileInputStream(file);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return;
				}
				
				if (outstanding.size() < 5) {

					byte[] data = new byte[1019];
					// determine what packet to send
					int number = lastAck + 1;
					while (true) {
						if (outstanding.contains(new Integer(number))) {
							number++;
						} else {
							break;
						}
					}

					try {
						if (fis.read(data) >= data.length) {
							map.put(new Integer(number), data);
							SendPacket sp = new SendPacket(number, false);
							sp.start();
							outstanding.add(new Integer(number));
						} else {
							map.put(new Integer(number), data);
							SendPacket sp = new SendPacket(number, true);
							sp.start();
							outstanding.add(new Integer(number));
							lastAckNumber = number;
							try {
								fis.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private class NoFilePacket extends Thread {
		private Header h;

		public void run() {
			h = new Header(false, false, true, false, 0);
			byte[] sendData = h.setAndGetData();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, clientIP, clientPort);
			try {
				serverSocket.send(sendPacket);
				System.out
						.println("Sent packet to client informing on file not found");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class SendPacket extends Thread {
		private byte[] data;
		private int index;
		private boolean last;

		public SendPacket(int index, boolean last) {
			this.index = index;
			this.last = last;
			this.data = map.get(new Integer(index));
		}

		public void run() {
			byte[] sendData = new byte[data.length + 5];
			Header h = new Header(false, false, last, true, index);
			byte[] headData = h.setAndGetData();
			for (int i = 0; i < h.SIZE; i++) {
				sendData[i] = headData[i];
			}

			for (int i = 5; i < sendData.length; i++) {
				sendData[i] = sendData[i - 5];
			}

			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, clientIP, clientPort);
			try {
				serverSocket.send(sendPacket);
				System.out
						.println("Send packet number " + index + " to client");
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
