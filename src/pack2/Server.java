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
	private boolean cSend;
	private NoFilePacketSend nf;
	// TODO
	/*
	 * we also have to worry about timeouts.
	 * right now it is just removing what it can from the outstanding list
	 * and trying again.
	 */
	public static void main(String[] args) {
		new Server(args[0]);
	}

	public Server(String port) {
		try {
			cSend = true;
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
			serverSocket.setSoTimeout(3000);
			s = null;
			nf = null;
			r = new Receive();
			r.start();

			while (r.isAlive()) {

			}

			if (s != null) {
				while (s.isAlive()) {

				}
			}
			
			if (nf != null) {
				while (nf.isAlive()) {

				}
			}
			System.out.println("All acks received, server closing");
			serverSocket.close();
			return;
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
						return;
					} else {
						nf = new NoFilePacketSend();
						lastAckNumber = 0;
						nf.start();
						gotAcksLoop();
						return;
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
			return false;
		}

		private boolean getFirstConnection() {
			
			while(true) {
				
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
			
			if(noError(receivePacket.getData())) {
				// get clientPort and clientIP
				clientPort = receivePacket.getPort();
				clientIP = receivePacket.getAddress();

				// get file name
				fileName = new String(receivePacket.getData(), 0,
						receivePacket.getLength());

				lastAck++;
				System.out.println("Received packet from " + clientIP + ":"
						+ clientPort + " asking for " + fileName);
				break;
			} else {
				System.out.println("Received corrupted packet from client");
			}
			
			}
			return true;
		}

		public void gotAcksLoop() {

			while (!lastAckReceived) {
				updateLastAck();
				if (outstanding.size() > 0) {
					byte[] receiveData = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(
							receiveData, receiveData.length);

					// Receive packet
					try {
						serverSocket.receive(receivePacket);
						int seqNum = 0;
						for (int i = 2; i < receivePacket.getData().length; i++) {
							int x = (int) receivePacket.getData()[i];
							seqNum += x;
						}
						if (noError(receivePacket.getData())) {
							if (getAcks(seqNum)) {
								updateLastAck();
								return;
							}
						} else {
							System.out.println("Ack packet received, but bad checksum");
							while(outstanding.size() > 0) {
								outstanding.remove(0);
							}
							updateLastAck();
							cSend = true;
						}
					} catch (Exception e) {
						// timeout has occured
						// so we do not ack their ack,
						// meaning the packet will be sent again
						updateLastAck();
						// clear outstanding so we can send again
						while(outstanding.size() > 0) {
							outstanding.remove(0);
						}
						cSend = true;
					}
				}
			}
		}

		private boolean noError(byte[] input) {
			String check1 = byteToBitString(input[0])
					+ byteToBitString(input[1]);
			byte[] i2 = new byte[input.length - 2];
			byte[] i3 = null;
			for(int i = 0; i < i2.length; i++){
				byte b = input[i + 2];
				i2[i] = b;
				if(b != 0){
					i2[i] = b;
				} else {
					i3 = new byte[i];
					for(int j = 0; j < i3.length; j++){
						i3[j] = i2[j];
					}
					break;
				}
			}
			
			String check2 = Header.genCheckSumStatic(i2);
			return trimAndCheck(check1, check2);
		}
		
		private boolean trimAndCheck(String one, String two) {
			int index = one.indexOf("1");
			one = one.substring(index);
			
			index = two.indexOf("1");
			two = two.substring(index);
			
			return one.equals(two);
		}

		private String byteToBitString(byte b) {
			return ("0000000" + Integer.toBinaryString(0xFF & b)).replaceAll(
					".*(.{8})$", "$1");
		}

		public boolean getAcks(int seq) {
			System.out.println("Got Ack for packet number " + seq);
			if (!gotAcks.contains(new Integer(seq))) {
				gotAcks.add(new Integer(seq));
			}
			if (outstanding.contains(new Integer(seq))) {
				outstanding.remove(new Integer(seq));
			}
			
			updateLastAck();
			if (lastAck == lastAckNumber) {
				lastAckReceived = true;
				return true;
			} else {
				return false;
			}
		}
		
		private void updateLastAck(){
			for(int i = lastAck+1; i <= gotAcks.size(); i++){
				if(gotAcks.contains(new Integer(i))){
					lastAck = i;
				} else
					break;
			}
			if(lastAck == lastAckNumber){
				lastAckReceived = true;
			}
		}
	}

	private class Send extends Thread {
		private boolean canSend() {
			return cSend;
		}

		public void run() {
			FileInputStream fis = null;
			while (r.isAlive()) {
				while (true) {
					if (!r.isAlive()) {
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return;
					}
					if (canSend()) {
						break;
					}
				}
				try {
					if (fis == null) {
						fis = new FileInputStream(file);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return;
				}

				if (outstanding.size() < 5) {
					int lastNumber = -1;
					byte[] data = new byte[1024 - (new Header()).SIZE];
					// determine what packet to send
					int number = lastAck + 1;
					while (true) {
						if(lastAckReceived){
							return;
						}
						
						if(number > lastAck + 5){
							number = lastAck+1;
						}
						if (outstanding.contains(new Integer(number))) {
							number++;
						} else if (gotAcks.contains(new Integer(number))) {
							number++;
						} else if((lastAck + 5) < number){
							number = lastAck + 1;
						} else if (!canSend()){
							continue;
						} else if (!r.isAlive() || lastAckReceived) {
							return;
						} else {
							if(number == lastNumber || number > lastAckNumber){
								number = lastAck + 1;
							} else {
								break;
							}
						}
					}

					try {

						if (map.containsKey(new Integer(number))) {
							if (lastAck == number) {
								cSend = false;
								SendPacket sp = new SendPacket(number, true);
								sp.start();
								outstanding.add(new Integer(number));
							} else {
								SendPacket sp = new SendPacket(number, false);
								sp.start();
								outstanding.add(new Integer(number));
							}
							lastNumber = number;
						} else {
							int l = fis.read(data);
							if (l >= data.length) {
								map.put(new Integer(number), data);
								SendPacket sp = new SendPacket(number, false);
								sp.start();
								outstanding.add(new Integer(number));
								lastNumber = number;
							} else if(l > 0){
								map.put(new Integer(number), data);
								cSend = false;
								SendPacket sp = new SendPacket(number, true);
								sp.start();
								outstanding.add(new Integer(number));
								lastAckNumber = number;
								lastNumber = number;
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private class NoFilePacketSend extends Thread {
		public void run() {
			while(r.isAlive()) {
				if(cSend) {
					cSend = false;
					NoFilePacket nfp = new NoFilePacket();
					nfp.start();
				}
			}
		}
	}

	private class NoFilePacket extends Thread {
		private Header h;

		public void run() {
			h = new Header(false, false, true, false, 0);
			byte[] zero = new byte[0];
			h.generateChecksum(zero);
			byte[] sendData = h.setAndGetData();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, clientIP, clientPort);
			try {
				serverSocket.send(sendPacket);
				System.out
						.println("Sent packet to client informing on file not found");
				outstanding.add(new Integer(0));
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
			Header h = new Header(false, false, last, true, index);
			h.generateChecksum(data);
			byte[] sendData = new byte[data.length + h.SIZE];
			byte[] headData = h.setAndGetData();
			for (int i = 0; i < h.SIZE; i++) {
				sendData[i] = headData[i];
			}

			for (int i = h.SIZE; i < sendData.length; i++) {
				sendData[i] = data[i - h.SIZE];
			}

			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, clientIP, clientPort);
			try {
				serverSocket.send(sendPacket);
				System.out
						.println("Send packet number " + index + " to client");
			} catch (IOException e) {
				outstanding.remove(new Integer(index));
				cSend = true;
				// i am thinking that a timeout on the send or
				// another error will occur here, so we will just
				// say that we are no longer trying to send the packet so
				// we can re send it above
				e.printStackTrace();
			}
		}
	}
}

