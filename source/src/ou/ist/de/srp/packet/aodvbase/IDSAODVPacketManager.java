package ou.ist.de.srp.packet.aodvbase;


import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import java.util.Random;

import javax.crypto.Cipher;

import ou.ist.de.srp.algo.AbstractAlgorithm;
import ou.ist.de.srp.algo.idsaodv.IDSAODVAlgorithm;
import ou.ist.de.srp.node.Node;
import ou.ist.de.srp.packet.PacketManager;

public class IDSAODVPacketManager extends PacketManager<IDSAODVPacket> {
	
	protected String keyFile;
	protected String keySize;	

	protected int p;
	protected int g;
	protected int r1;
	
	protected IDSAODVAlgorithm idsalg;
	public IDSAODVPacketManager() {
		p=17;
		g=19;
		Random rnd = new Random();
		r1 = rnd.nextInt(10) + 10;
	}
	
	public String getKeyFile() {
		return keyFile;
	}

	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}

	public String getKeySize() {
		return keySize;
	}

	public void setKeySize(String keySize) {
		this.keySize = keySize;
	}

	
	@Override
	public void setAlgorithm(AbstractAlgorithm<IDSAODVPacket> algorithm) {
		// TODO Auto-generated method stub
		super.setAlgorithm(algorithm);
		idsalg=(IDSAODVAlgorithm)algorithm;
	}

	@Override
	protected IDSAODVPacket addExtraInfoToInitialRequestPacket(IDSAODVPacket pkt, Node<IDSAODVPacket> node) {
		// TODO Auto-generated method stub
		pkt.ids=idsalg.getIdHash(pkt.getSrc().getAddress());
		pkt.idd=idsalg.getIdHash(pkt.getDest().getAddress());
		pkt.g=g;
		pkt.p=p;
		pkt.r2=doEncryptDecryptData(genR1ByteArray(),true,idsalg.getKeyPair(pkt.getSrc().getAddress()[3]));
		pkt.ri.add(pkt.ids);
		pkt.sig=alg.sign(pkt);
		pkt.b=new byte[] {0};
		return pkt;
	}
	
	@Override
	protected IDSAODVPacket addExtraInfoToInitialReplyPacket(IDSAODVPacket snd, IDSAODVPacket rcv,
			Node<IDSAODVPacket> node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IDSAODVPacket addExtraInfoToForwardingRequestPacket(IDSAODVPacket pkt, Node<IDSAODVPacket> node) {
		// TODO Auto-generated method stub
		
		pkt.ri.add(idsalg.getIdHash(pkt.getSndr().getAddress()));
		byte[] bytes=new byte[pkt.b.length+1];
		for(int i=0;i<pkt.b.length;i++) {
			bytes[i]=pkt.b[i];
		}
		pkt.b=bytes;
		pkt.sig=alg.sign(pkt);
		return pkt;
	}

	@Override
	protected IDSAODVPacket addExtraInfoToForwardingReplyPacket(IDSAODVPacket pkt, Node<IDSAODVPacket> node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IDSAODVPacket generatePlainPacket() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean alreadyReceived(IDSAODVPacket pkt, Node<IDSAODVPacket> node) {
		// TODO Auto-generated method stub
		return false;
	}
	protected byte[] genR1ByteArray() {
		BigInteger G=BigInteger.valueOf(g);
		BigInteger P=BigInteger.valueOf(p);
		
		BigInteger R1=G.pow(r1).mod(P);
		return R1.toByteArray();
	}
	protected byte[] doEncryptDecryptData(byte[] data,boolean encrypt,KeyPair kp) {
		byte[] ret=null;
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			if(encrypt) {
			cipher.init(Cipher.ENCRYPT_MODE, kp.getPrivate());
			}
			else {
				cipher.init(Cipher.DECRYPT_MODE, kp.getPublic());
			}
			ret = cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

}
