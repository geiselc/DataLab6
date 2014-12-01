package pack2;

public class Header {

	public final int SIZE = 7; // in bytes
	private byte[] data; // header data
	private boolean ack;
	private boolean rst;
	private boolean fin;
	private boolean fileExists;
	private int seq;
	private String checkSum;

	public Header() {
		data = new byte[SIZE];
	}


	public Header(boolean ack, boolean rst, boolean fin, boolean fileExists,
			int seq) {
		this.ack = ack;
		this.rst = rst;
		this.fin = fin;
		this.fileExists = fileExists;
		this.seq = seq;
		this.data = new byte[SIZE];
	}

	public Header(byte[] bytes) {
		data = bytes;
		decodeData();
	}

	public boolean isAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public boolean isRst() {
		return rst;
	}

	public void setRst(boolean rst) {
		this.rst = rst;
	}

	public boolean isFin() {
		return fin;
	}

	public void setFin(boolean fin) {
		this.fin = fin;
	}

	public boolean fileExists() {
		return fileExists;
	}

	public void setFileExists(boolean fileExists) {
		this.fileExists = fileExists;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public boolean isFileExists() {
		return fileExists;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String toString() {
		return this.ack + " " + this.rst + " " + this.fin + " " + this.seq;
	}

	/*
	 * data in header by index: 0 ack : boolean : 0 = false, 1 = true 1 rst :
	 * boolean : 0 = false, 1 = true 2 fin : boolean : 0 = false, 1 = true 3
	 * fileExists : boolean : 0 = false, 1 = true 4 seq1 5 seq2 6 seq3 7 seqOff
	 */
	public byte[] getData() {
		return data;
	}

	public byte[] setAndGetData() {
		for (int i = 0; i < SIZE; i++) {
			switch (i) {
			case 0:
				if (ack) {
					data[i] = (byte) 1;
				} else {
					data[i] = (byte) 0;
				}
				break;
			case 1:
				if (rst) {
					data[i] = (byte) 1;
				} else {
					data[i] = (byte) 0;
				}
				break;
			case 2:
				if (fin) {
					data[i] = (byte) 1;
				} else {
					data[i] = (byte) 0;
				}
				break;
			case 3:
				if (fileExists) {
					data[i] = (byte) 1;
				} else {
					data[i] = (byte) 0;
				}
				break;
			case 4:
				data[i] = (byte) seq;
				break;
			case 5:
				String str1 = this.checkSum.substring(0, 7);
				data[i] = (byte) Integer.parseInt(str1, 2);
				break;
			case 6:
				String str2 = this.checkSum.substring(8);
				data[i] = (byte) Integer.parseInt(str2, 2);
				break;
			}
		}
		return data;
	}

	public void decodeData() {
		for (int i = 0; i < SIZE; i++) {
			byte b = data[i];
			boolean one = (b == (byte) 1);
			switch (i) {
			case 0:
				ack = one;
				break;
			case 1:
				rst = one;
				break;
			case 2:
				fin = one;
				break;
			case 3:
				fileExists = one;
				break;
			case 4:
				seq = (int) b;
				break;
			case 5:
				String str1 = byteToBitString(b);
				this.checkSum = str1;
				break;
			case 6:
				String str2 = byteToBitString(b);
				this.checkSum =  this.checkSum + str2;
				break;
			}
		}
	}
	
	private String byteToBitString(byte b) {
		return ("0000000" + Integer.toBinaryString(0xFF & b)).replaceAll(
				".*(.{8})$", "$1");
	}
	public static String byteToBitStringStatic(byte b) {
		return ("0000000" + Integer.toBinaryString(0xFF & b)).replaceAll(
				".*(.{8})$", "$1");
	}

	public String getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}

	public void generateChecksum(byte[] input) {
		byte[] i2 = new byte[SIZE - 2];
		for (int i = 0; i < i2.length; i++) {
			switch (i) {
			case 0:
				if (ack) {
					i2[i] = (byte) 1;
				} else {
					i2[i] = (byte) 0;
				}
				break;
			case 1:
				if (rst) {
					i2[i] = (byte) 1;
				} else {
					i2[i] = (byte) 0;
				}
				break;
			case 2:
				if (fin) {
					i2[i] = (byte) 1;
				} else {
					i2[i] = (byte) 0;
				}
				break;
			case 3:
				if (fileExists) {
					i2[i] = (byte) 1;
				} else {
					i2[i] = (byte) 0;
				}
				break;
			case 4:
				i2[i] = (byte) seq;
				break;
			}
		}
		byte[] i3 = new byte[i2.length + input.length];
		for (int i = 0; i < i2.length; i++) {
			i3[i] = i2[i];
		}
		for (int i = 0; i < input.length; i++) {
			i3[i2.length + i] = input[i];
		}
		this.setCheckSum(genCheckSum(i3));
	}

	private String genCheckSum(byte[] input) {
		byte[] buf = input;
		int length = buf.length;
		int i = 0;
		long sum = 0;
		while (length > 0) {
			sum += (buf[i++] & 0xff) << 8;
			if ((--length) == 0)
				break;
			sum += (buf[i++] & 0xff);
			--length;
		}

		long x = (~((sum & 0xFFFF) + (sum >> 16))) & 0xFFFF;
		String str = Long.toBinaryString(x);
		while(str.length() < 16) {
			str = "0"+str;
		}
		return str;
	}
	

	public static String genCheckSumStatic(byte[] input) {
		byte[] buf = input;
		int length = buf.length;
		int i = 0;
		long sum = 0;
		while (length > 0) {
			sum += (buf[i++] & 0xff) << 8;
			if ((--length) == 0)
				break;
			sum += (buf[i++] & 0xff);
			--length;
		}

		long x = (~((sum & 0xFFFF) + (sum >> 16))) & 0xFFFF;
		String str = Long.toBinaryString(x);
		while(str.length() < 16) {
			str = "0"+str;
		}
		return str;
	}
}