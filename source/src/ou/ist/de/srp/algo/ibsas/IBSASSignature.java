package ou.ist.de.srp.algo.ibsas;

import java.nio.ByteBuffer;

import it.unisa.dia.gas.jpbc.Element;

public class IBSASSignature {
	protected Element sig1;
	protected Element sig2;
	protected Element sig3;

	public byte[] toBytes() {
		byte[] s1 = sig1.toBytes();
		byte[] s2 = sig2.toBytes();
		byte[] s3 = sig3.toBytes();
		ByteBuffer bb = ByteBuffer.allocate(s1.length + s2.length + s3.length + Integer.BYTES * 3);
		bb.putInt(s1.length);
		bb.put(s1);
		bb.putInt(s2.length);
		bb.put(s2);
		bb.putInt(s3.length);
		bb.put(s3);
		// System.out.println("signature to byte size="+bb.capacity());
		return bb.array();
	}

	public void setSig(int index, Element e) {
		switch (index) {
		case 0: {
			sig1 = e;
			break;
		}
		case 1: {
			sig2 = e;
			break;
		}
		case 2: {
			sig3 = e;
			break;
		}
		}
	}

	public String toString() {
		String ret = "sig1:" + sig1.toString() + "\n";
		ret += "sig2:" + sig2.toString() + "\n";
		ret += "sig3:" + sig3.toString() + "\n";
		return ret;
	}

}
