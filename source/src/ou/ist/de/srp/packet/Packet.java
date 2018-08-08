package ou.ist.de.srp.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import ou.ist.de.srp.Constants;

public abstract class Packet {
	protected byte type;
	protected int seq;
	protected InetAddress src;
	protected InetAddress dest;
	protected InetAddress sndr;
	protected InetAddress next;
	protected int hops;
	
	public Packet(){
		type=0;
		src=null;
		dest=null;
		hops=0;
	}
	
	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public InetAddress getSrc() {
		return src;
	}

	public void setSrc(InetAddress src) {
		this.src = src;
	}

	public InetAddress getDest() {
		return dest;
	}

	public void setDest(InetAddress dest) {
		this.dest = dest;
	}

	public int getHops() {
		return hops;
	}

	public void setHops(int hops) {
		this.hops = hops;
	}

	public InetAddress getSndr() {
		return sndr;
	}

	public void setSndr(InetAddress sndr) {
		this.sndr = sndr;
	}

	public InetAddress getNext() {
		return next;
	}

	public void setNext(InetAddress next) {
		this.next = next;
	}
	public int getPacketLength() {
		return 1//type
				+4//seq
				+4*Constants.InetAddressLength
				+4//hops
				+getExtraPacketLength();
		
	}
	protected abstract int getExtraPacketLength();
	protected abstract ByteBuffer extraToByteArray(ByteBuffer bb);
	protected abstract void extraFromByteArray(ByteBuffer bb);
	
	protected ByteBuffer toByteArray(ByteBuffer bb) {
		try {
			bb.put(type);
			bb.putInt(seq);
			bb.put(src.getAddress());
			bb.put(dest.getAddress());
			bb.put(sndr.getAddress());
			bb.put(next.getAddress());
			bb.putInt(hops);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return bb;
	}
	protected ByteBuffer fromByteArray(ByteBuffer bb) {
		byte[] addr=new byte[Constants.InetAddressLength];
		try {
			type=bb.get();
			seq=bb.getInt();
			bb.get(addr);
			src = InetAddress.getByAddress(addr);
			System.out.println("src "+src.toString());
			bb.get(addr);
			dest = InetAddress.getByAddress(addr);
			bb.get(addr);
			sndr = InetAddress.getByAddress(addr);
			bb.get(addr);
			next = InetAddress.getByAddress(addr);
			hops = bb.getInt();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return bb;
	}
	public byte[] toByteArray() {
		ByteBuffer bb;
		int length=getPacketLength();
		if(src==null) {
			return null;
		}
		
		try {
			bb=ByteBuffer.allocate(length);
			bb=toByteArray(bb);
			bb=extraToByteArray(bb);
			return bb.array();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public void fromByteArray(byte[] b) {
		ByteBuffer bb;
		try {
			bb=ByteBuffer.wrap(b);
			fromByteArray(bb);
			extraFromByteArray(bb);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	public void fromDatagramPacket(DatagramPacket dp) {
		try {
			byte[] b=dp.getData();
			fromByteArray(b);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	public String toString() {
		String ret="type:"+type;
		ret+=" src:"+src.toString();
		ret+=" dest:"+dest.toString();
		ret+=" next:"+((next==null)?"null":next.toString());
		ret+=" sndr:"+((sndr==null)?"null":sndr.toString());
		ret+=" hops:"+hops+" seq:"+seq+"\n";
		return ret;
	}
}
