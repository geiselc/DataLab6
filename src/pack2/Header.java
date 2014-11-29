package pack2;
public class Header {

	public final int SIZE = 5; // in bytes
	private byte[] data;				// header data
	private boolean ack;
	private boolean rst;
	private boolean fin;
	private boolean fileExists;
	private int seq;

	public Header() {
		data = new byte[SIZE];
	}
	
	public Header(boolean ack, boolean rst, boolean fin, boolean fileExists, int seq){
		this.ack = ack;
		this.rst = rst;
		this.fin = fin;
		this.fileExists = fileExists;
		this.seq = seq;
		data = new byte[SIZE];
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
	
	public String toString(){
		return this.ack+" "+this.rst+" "+this.fin+" "+this.seq;
	}
	/* data in header by index:
	 * 0	ack : boolean : 0 = false, 1 = true
	 * 1	rst : boolean : 0 = false, 1 = true
	 * 2	fin : boolean : 0 = false, 1 = true
	 * 3	fileExists : boolean : 0 = false, 1 = true
	 * 4	seq
	*/
	public byte[] getData() {
		return data;
	}
	public byte[] setAndGetData() {
		for(int i = 0; i < SIZE; i++) {
			switch(i) {
			case 0:
				if(ack) {
					data[i] = (byte)1;
				} else {
					data[i] = (byte)0;
				}
				break;
			case 1:
				if(rst) {
					data[i] = (byte)1;
				} else {
					data[i] = (byte)0;
				}
				break;
			case 2:
				if(fin) {
					data[i] = (byte)1;
				} else {
					data[i] = (byte)0;
				}
				break;
			case 3:
				if(fileExists) {
					data[i] = (byte)1;
				} else {
					data[i] = (byte)0;
				}
				break;
			default:
				data[i] = (byte)seq;
				break;
			}
		}
		return data;
	}
	public void decodeData() {
		for(int i = 0; i < SIZE; i++) {
			byte b = data[i];
			boolean one = (b == (byte)1);
			switch(i) {
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
			default:
				seq = (int)b;
				break;
			}
		}
	}
}