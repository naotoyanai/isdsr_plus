package ou.ist.de.srp.packet.aodvbase;

import java.nio.ByteBuffer;

import ou.ist.de.srp.packet.Packet;
import ou.ist.de.srp.packet.RouteStrage;
public class IDSAODVPacket extends Packet{

	protected RouteInfo ri;
	protected byte[] ids;
	protected byte[] idd;
	protected int p;
	protected int g;
	protected byte[] sig;
	protected byte[] r2;
	protected byte[] b;
	
	public IDSAODVPacket() {
		ri=new RouteInfo();
	}
	
	public RouteInfo getRi() {
		return ri;
	}

	public void setRi(RouteInfo ri) {
		this.ri = ri;
	}

	public byte[] getIds() {
		return ids;
	}

	public void setIds(byte[] ids) {
		this.ids = ids;
	}

	public byte[] getIdd() {
		return idd;
	}

	public void setIdd(byte[] idd) {
		this.idd = idd;
	}

	public int getP() {
		return p;
	}

	public void setP(int p) {
		this.p = p;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public byte[] getSig() {
		return sig;
	}

	public void setSig(byte[] sig) {
		this.sig = sig;
	}

	public byte[] getR2() {
		return r2;
	}

	public void setR2(byte[] r2) {
		this.r2 = r2;
	}

	public byte[] getB() {
		return b;
	}

	public void setB(byte[] b) {
		this.b = b;
	}

	@Override
	protected int getExtraPacketLength() {
		// TODO Auto-generated method stub
		
		return Integer.BYTES+ids.length
				+Integer.BYTES+idd.length
				+Integer.BYTES//p
				+Integer.BYTES//g
				+Integer.BYTES//the length of r2
				+r2.length
				+Integer.BYTES//the length of ri
				+ri.dataLength()+Integer.BYTES+sig.length
				+Integer.BYTES+b.length;
	}

	@Override
	protected ByteBuffer extraToByteArray(ByteBuffer bb) {
		// TODO Auto-generated method stub
		try {
			bb.putInt(ids.length);
			bb.put(ids);
			bb.putInt(idd.length);
			bb.put(idd);
			bb.putInt(p);
			bb.putInt(g);
			bb.putInt(r2.length);
			bb.put(r2);
			ri.toByteArray(bb);
			bb.putInt(sig.length);
			bb.put(sig);
			bb.putInt(b.length);
			bb.put(b);
			return bb;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void extraFromByteArray(ByteBuffer bb) {
		// TODO Auto-generated method stub
		try {
			ids=new byte[bb.getInt()];
			bb.get(ids);
			idd=new byte[bb.getInt()];
			bb.get(idd);
			p=bb.getInt();
			g=bb.getInt();
			r2=new byte[bb.getInt()];
			bb.get(r2);
			ri.fromByteArray(bb);
			sig=new byte[bb.getInt()];
			bb.get(sig);
			b=new byte[bb.getInt()];
			bb.get(b);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	public byte[] genByteArrayForSignature(int limit) {
		if(limit>ri.getRiLength()) {
			return null;
		}
		ByteBuffer bb;
		int length=ids.length+idd.length+Integer.BYTES*4+r2.length;
		for(int i=0;i<ri.getRiLength();i++) {
			length+=ri.get(i).length;
		}
		try {
			bb=ByteBuffer.allocate(length);
			bb.put(ids);
			bb.putInt(seq);
			bb.putInt(limit);
			bb.put(idd);
			bb.put(r2);
			bb.putInt(p);
			bb.putInt(g);
			for(int i=0;i<limit;i++) {
				bb.put(ri.get(i));
			}
			return bb.array();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public class RouteInfo extends RouteStrage<byte[]>{
		
		@Override
		public int dataLength() {
			// TODO Auto-generated method stub.
			int ret=0;
			for(int i=0;i<al.size();i++) {
				ret+=Integer.BYTES+al.get(i).length;
			}
			return ret;
		}
		@Override
		public ByteBuffer toByteArray(ByteBuffer bb) {
			// TODO Auto-generated method stub
			try {
				bb.putInt(al.size());
				byte[] tmp=null;
				for(int i=0;i<al.size();i++) {
					tmp=al.get(i);
					bb.putInt(tmp.length);
					bb.put(tmp);
				}
				return bb;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		public void fromByteArray(ByteBuffer bb) {
			// TODO Auto-generated method stub
			int length;
			byte[] tmp;
			al.clear();
			try {
				length=bb.getInt();
				for(int i=0;i<length;i++) {
					tmp=new byte[bb.getInt()];
					bb.get(tmp);
					add(tmp);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public boolean find(byte[] data) {
			for(int i=0;i<al.size();i++) {
				if(isSame(data,al.get(i))) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public int findIndex(byte[] elem) {
			for(int i=0;i<al.size();i++) {
				if(isSame(elem,al.get(i))) {
					return i;
				}
			}
			return -1;
		}
		protected boolean isSame(byte[] x,byte[] y) {
			if(x.length!=y.length){
				return false;
			}
			for(int i=0;i<x.length;i++) {
				if(x[i]!=y[i]) {
					return false;
				}
			}
			return true;
		}
	}
	
	
}
