package ou.ist.de.srp.packet;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import ou.ist.de.srp.Constants;

public class FragmentPacket {
	protected InetAddress src;
	protected InetAddress dst;
	protected int seq;
	protected int index;
	protected int total;
	protected int datalen;
	protected byte[] data;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] toByteArray() {
		ByteBuffer bb;

		bb = ByteBuffer.allocate(Constants.InetAddressLength + 2 + Integer.BYTES * 4 + data.length);
		bb.put(src.getAddress());
		bb.put(dst.getAddress());
		bb.putInt(seq);
		bb.putInt(index);
		bb.putInt(total);
		bb.putInt(data.length);
		bb.put(data);
		return bb.array();
	}

	public void fromByteArray(byte[] b) {
		ByteBuffer bb = ByteBuffer.wrap(b);
		byte[] addr = new byte[Constants.InetAddressLength];
		try {
			bb.get(addr);
			src = InetAddress.getByAddress(addr);
			bb.get(addr);
			dst = InetAddress.getByAddress(addr);
			seq = bb.getInt();
			index = bb.getInt();
			total = bb.getInt();
			datalen=bb.getInt();
			data = new byte[datalen];
			bb.get(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean equalsTo(FragmentPacket fp) {
		if(!src.equals(fp.src)) return false;
		if(!dst.equals(fp.dst)) return false;
		if(seq!=fp.seq) return false;
		if(index!=fp.index) return false;
		if(total!=fp.total) return false;
		
		return true;
	}
}
