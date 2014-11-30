package pack2;
public class Header {

	public final int SIZE = 8; // in bytes
	private byte[] data;				// header data
	private boolean ack;
	private boolean rst;
	private boolean fin;
	private boolean fileExists;
	private int seq1;
	private int seq2;
	private int seq3;
	private int seqOff;
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
		this.data = new byte[SIZE];
		if(seq%2==0) {
			seqOff = 0;
		} else {
			seqOff = 1;
			seq-=1;
		}
		if(seq==0) {
			seq1=0;
			seq2=0;
			seq3=0;
		} else {
			if (seq <= 250) {
				seq1 = seq;
				seq2 = 1;
				seq3 = 1;
			} else {
				
			}
		}
		seq = seq1*seq2*seq3;
		seq+=seqOff;
		
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
	
	public int getSeq1() {
		return seq1;
	}

	public void setSeq1(int seq1) {
		this.seq1 = seq1;
	}

	public int getSeq2() {
		return seq2;
	}

	public void setSeq2(int seq2) {
		this.seq2 = seq2;
	}

	public int getSeq3() {
		return seq3;
	}

	public void setSeq3(int seq3) {
		this.seq3 = seq3;
	}

	public int getSeqOff() {
		return seqOff;
	}

	public void setSeqOff(int seqOff) {
		this.seqOff = seqOff;
	}

	public boolean isFileExists() {
		return fileExists;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String toString(){
		return this.ack+" "+this.rst+" "+this.fin+" "+this.seq;
	}
	/* data in header by index:
	 * 0	ack : boolean : 0 = false, 1 = true
	 * 1	rst : boolean : 0 = false, 1 = true
	 * 2	fin : boolean : 0 = false, 1 = true
	 * 3	fileExists : boolean : 0 = false, 1 = true
	 * 4	seq1
	 * 5	seq2
	 * 6	seq3
	 * 7	seqOff
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
			case 4:
				data[i] = (byte)seq1;
				break;
			case 5:
				data[i] = (byte)seq2;
				break;
			case 6:
				data[i] = (byte)seq3;
				break;
			default:
				data[i] = (byte)seqOff;
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
			case 4:
				seq1 = (int)b;
				break;
			case 5:
				seq2 = (int)b;
				break;
			case 6:
				seq3 = (int)b;
				break;
			default:
				seqOff = (int)b;
				break;
			}
		}
		int x = seq1*seq2*seq3;
		x+=seqOff;
		seq = x;
	}
}