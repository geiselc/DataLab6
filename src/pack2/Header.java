package pack2;


public class Header {

	//public static final int SIZE = 8; // in bytes
	//private byte[] data;				// header data
	private boolean ack;
	private boolean rst;
	private boolean fin;
	private boolean fileExists;
	private int seq;
	private int lastAck;

	public Header() {
	}
	
	public Header(boolean ack, boolean rst, boolean fin, boolean fileExists, int seq, int lastAck){
		this.ack = ack;
		this.rst = rst;
		this.fin = fin;
		this.fileExists = fileExists;
		this.seq = seq;
		this.lastAck = lastAck;
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
	
	public int getLastAck() {
		return lastAck;
	}

	public void setLastAck(int lastAck) {
		this.lastAck = lastAck;
	}
	
	public String toString(){
		return this.ack+" "+this.rst+" "+this.fin+" "+this.seq+" "+this.lastAck;
	}

}