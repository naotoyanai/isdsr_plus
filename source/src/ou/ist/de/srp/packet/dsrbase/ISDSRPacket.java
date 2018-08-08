package ou.ist.de.srp.packet.dsrbase;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class ISDSRPacket extends DSRBasePacket {

	protected byte[] sig;
	
	
	public byte[] getSig() {
		return sig;
	}

	public void setSig(byte[] sig) {
		this.sig = sig;
	}

	@Override
	protected int getExtraPacketLength() {
		// TODO Auto-generated method stub
		return super.getExtraPacketLength()+Integer.BYTES+sig.length;
	}

	@Override
	protected ByteBuffer extraToByteArray(ByteBuffer bb) {
		// TODO Auto-generated method stub
		super.extraToByteArray(bb);
		try {
			bb.putInt(sig.length);
			bb.put(sig);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return bb;
	}

	@Override
	protected void extraFromByteArray(ByteBuffer bb) {
		// TODO Auto-generated method stub
		super.extraFromByteArray(bb);
		int length;
		try {
			length=bb.getInt();
			sig=new byte[length];
			bb.get(sig);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ISDSRPacket p=new ISDSRPacket();
		try {
			p.dest=InetAddress.getByAddress(new byte[] {1,1,1,1});
			p.src=InetAddress.getByAddress(new byte[] {2,1,1,1});
			p.next=InetAddress.getByAddress(new byte[] {3,1,1,1});
			p.sndr=InetAddress.getByAddress(new byte[] {4,1,1,1});
			p.seq=1;
			p.hops=1;
			p.sig=new byte[] {2,2,2,2,2,2,2,2,2,2,2};
			p.ri.add(InetAddress.getByAddress(new byte[] {3,3,3,3}));
			System.out.println("packet length "+p.getPacketLength());
			byte[] data=p.toByteArray();
			for(int i=0;i<data.length;i++) {
				System.out.print(data[i]+" ");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
