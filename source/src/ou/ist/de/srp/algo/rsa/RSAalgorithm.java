package ou.ist.de.srp.algo.rsa;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.Signature;
import java.util.ArrayList;
import java.util.HashMap;

import ou.ist.de.srp.Constants;
import ou.ist.de.srp.algo.AbstractAlgorithm;
import ou.ist.de.srp.packet.dsrbase.DSRBasePacket.RouteInfo;
import ou.ist.de.srp.packet.dsrbase.RSABasePacket;

public class RSAalgorithm extends AbstractAlgorithm<RSABasePacket> {
	protected KeyPair kp;
	protected Signature sig;
	protected int keySize;
	protected ArrayList<byte[]> signs;
	
	public RSAalgorithm(HashMap<String,String> params) {
		super(params);
		String keyFile=params.get("keyFile");
		String keySize=params.get("keySize");
		String index=params.get("index");
		setKeys(keyFile,index,keySize);
	}
	protected void setKeys(String keyFile,String strindex,String strkeysize) {
		try {
			int index=Integer.valueOf(strindex);
			System.out.println("key size="+strkeysize);
			int keySize=Integer.valueOf(strkeysize);
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyFile));
			KeyPair[] kparray = ((KeyPair[]) ois.readObject());
			kp = kparray[index];
			this.keySize = keySize/8;
			sig = Signature.getInstance("MD5WithRSA");
			signs = new ArrayList<byte[]>();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] sign(RSABasePacket pkt) {
		// TODO Auto-generated method stub
		byte[] sigbyte=pkt.getSig();
		long tmp;
		long time=System.currentTimeMillis();
		ByteBuffer bb = ByteBuffer.allocate(pkt.getRi().dataLength()+((sigbyte==null)?0:sigbyte.length));
		
		for(int i=0;i<pkt.getRi().getRiLength();i++) {
			bb.put(pkt.getRi().get(i).getAddress());
		}
		if(sigbyte!=null) {
			generateSignatureFromBytes(sigbyte);
			bb.put(sigbyte);
		}
		tmp=System.currentTimeMillis()-time;
		System.out.println("step 1:"+tmp);
		
		time=System.currentTimeMillis();
		byte[] data = bb.array();
		//showByteData(data);
		try {
			time=System.currentTimeMillis();
			sig.initSign(kp.getPrivate());
			sig.update(data);
			tmp=System.currentTimeMillis()-time;
			System.out.println("step 2-1:"+tmp);
			signs.add(sig.sign());
		} catch (Exception e) {
			e.printStackTrace();
		}
		tmp=System.currentTimeMillis()-time;
		System.out.println("step 2:"+tmp);

		time=System.currentTimeMillis();
		bb=ByteBuffer.allocate(signs.size()*keySize);
		for(int i=0;i<signs.size();i++) {
			System.out.println("sign "+i+" length="+signs.get(i).length);
			bb.put(signs.get(i));
		}
		tmp=System.currentTimeMillis()-time;
		System.out.println("step 3:"+tmp);
		return bb.array();
	}
	protected RSABasePacket checkPacketFormat(RSABasePacket pkt) {
		int length = pkt.getRi().getRiLength();
		ByteBuffer bb=ByteBuffer.wrap(pkt.getSig());
		byte[] sigbyte=new byte[length*keySize];
		bb.get(sigbyte);
		pkt.setSig(sigbyte);
		return pkt;
	}
	@Override
	public boolean verify(RSABasePacket pkt) {
		boolean ret = true;
		//checkPacketFormat(pkt);
		generateSignatureFromBytes(pkt.getSig());
		RouteInfo ri=pkt.getRi();
		ByteBuffer bb;
		int dataLength;
		byte[] data;
		for(int i=0;ret&&(i<(signs.size()));i++) {
			dataLength=(i+1)*Constants.InetAddressLength+i*keySize;
			bb=ByteBuffer.allocate(dataLength);
			for(int j=0;j<=i;j++) {
				System.out.println("ri get("+j+") "+ri.getAddressByString(j));
				bb.put(ri.get(j).getAddress());
			}
			for (int j = 0; j < i ; j++) {
				bb.put(signs.get(j));
			}
			data=bb.array();
			//showByteData(data);
			try {
				sig.initVerify(kp.getPublic());
				sig.update(data);
				ret &= sig.verify(signs.get(i));
				System.out.println("\ncount="+i+" result "+ret);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	public void generateSignatureFromBytes(byte[] bytes) {
		// TODO Auto-generated method stub
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		signs.clear();
		byte[] sign = null;
		while (bb.hasRemaining()) {
			sign = new byte[keySize];
			bb.get(sign);
			signs.add(sign);
		}
	}
	public void showByteData(byte[] data) {
		System.out.println("start showing");
		for(int i=0;i<data.length;i++) {
			System.out.print(" "+data[i]);
		}
	}
}
