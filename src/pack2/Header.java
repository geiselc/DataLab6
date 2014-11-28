package pack2;

public class Header {

	//public static final int SIZE = 8; 	// in bytes
	private byte[] data;				// header data
	private boolean syn;
	private boolean ack;
	private boolean rst;
	private boolean fin;
	private boolean fileNotExists;
	private int seq;
	private int lastAck;

	public Header() {
	}
	
	public Header(boolean syn, boolean ack, boolean rst, boolean fin, boolean fileNotExists, int seq){
		this.syn = syn;
		this.ack = ack;
		this.rst = rst;
		this.fin = fin;
		this.seq = seq;
	}
	
	/**
	 * TODO
	 * Track syn, ack, req, fin and seq num
	 * syn - has connection been established?
	 * ack - has packet been acknowledged?
	 * rst - if error occurs, reset
	 * fin - no more data from sender
	 * fileNotExists - used to inform client if they request a file that doesn't exist on the server
	 * seq - current number in sequence - for in-order delivery
	 */
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public boolean isSyn() {
		return syn;
	}

	public void setSyn(boolean syn) {
		this.syn = syn;
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

	public void setRst(boolean req) {
		this.rst = rst;
	}

	public boolean isFin() {
		return fin;
	}

	public void setFin(boolean fin) {
		this.fin = fin;
	}
	
	public boolean fileNotExists() {
		return fileNotExists;
	}

	public void setFileNotExists(boolean fileNotExists) {
		this.fileNotExists = fileNotExists;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}
	
	public int getLastAck() {
		return lastAck;
	}

	public void setLastAck(int lastAck) {
		this.lastAck = lastAck;
	}

}
