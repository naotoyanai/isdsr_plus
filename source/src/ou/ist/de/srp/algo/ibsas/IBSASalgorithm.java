package ou.ist.de.srp.algo.ibsas;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HashMap;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.ElementPowPreProcessing;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import ou.ist.de.srp.algo.AbstractAlgorithm;
import ou.ist.de.srp.packet.dsrbase.ISDSRPacket;

public class IBSASalgorithm extends AbstractAlgorithm<ISDSRPacket> {

	protected Pairing pairing;
	protected MPK mpk;
	protected MSK msk;
	protected ISK isk;
	protected ElementPowPreProcessing mpkg1;
	protected ElementPowPreProcessing isksk1;

	enum HashType {
		H1, H2, H3
	};

	public IBSASalgorithm(HashMap<String, String> params) {
		super(params);
		String paramFile = params.get("paramFile");
		String keyParamFile = params.get("keyParamFile");
		String index = params.get("index");
		String usePBC = params.get("usePBC");
		String uid = params.get("uid");
		
		System.out.println("ibsas algorithm constructor");
		
		if (usePBC != null && !usePBC.equalsIgnoreCase("false")) {
			
			PairingFactory.getInstance().setUsePBCWhenPossible(true);
		}
		if (paramFile != null) {
			setPairing(paramFile);

			System.out.println("use pbc:"+PairingFactory.getInstance().isPBCAvailable());
			if (keyParamFile != null && index != null) {
				setKeys(keyParamFile, index);
				keyDerivation(uid);
			}
		} else {
			System.out.println("No parameters were selected.");
			System.exit(0);
		}
	}

	protected void setPairing(String paramFile) {
		pairing = PairingFactory.getPairing(paramFile);
	}

	protected void setKeys(String keyParamFile, String indexstr) {
		try {
			int index = Integer.valueOf(indexstr);
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyParamFile));
			MasterKey mk = (MasterKey) ois.readObject();
			ois.close();
			mpk = new MPK();
			msk = new MSK();
			// ===== set up =====//
			mpk.g1 = pairing.getG1().newElementFromBytes(mk.ale[index].g);
			msk.a1 = pairing.getZr().newElementFromBytes(mk.ale[index].a1);
			msk.a2 = pairing.getZr().newElementFromBytes(mk.ale[index].a2);
			mpk.g2 = mpk.g1.duplicate().powZn(msk.a1);
			mpk.g3 = mpk.g1.duplicate().powZn(msk.a2);
			isk = new ISK();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setKeys(MPK mpk, MSK msk) {
		this.mpk = mpk;
		this.msk = msk;
		isk = new ISK();
	}

	public void keyDerivation(String uid) {

		System.out.println("key derivation uid=" + uid);
		Element e1 = Hash(HashType.H1, uid);// Hash1(uidByte);
		Element e2 = Hash(HashType.H2, uid);// Hash2(uidByte);
		// System.out.println("generate sk " + uid);

		isk.sk1 = e1.powZn(msk.a1);
		isk.sk2 = e2.powZn(msk.a2);
		mpkg1 = mpk.g1.getElementPowPreProcessing();
		isksk1 = isk.sk1.getElementPowPreProcessing();
	}

	@Override
	public byte[] sign(ISDSRPacket pkt) {
		int len = pkt.getRi().getRiLength();
		String uid = pkt.getRi().getAddressByString(len - 1);
		String msg = pkt.getRi().getAddrSequence();
		IBSASSignature sig = generateSignatureFromBytes(pkt.getSig());

		System.out.println("sign:uid=" + uid + " msg=" + msg);
		Element r, x;
		long times[] = new long[15];
		times[0] = System.currentTimeMillis();
		System.out.println("start signing");
		// ElementPowPreProcessing pppmpkg1=mpk.g1.getElementPowPreProcessing();
		// Element immpkg1=mpk.g1.getImmutable();
		r = pairing.getZr().newRandomElement();
		times[1] = System.currentTimeMillis();
		x = pairing.getZr().newRandomElement();
		times[2] = System.currentTimeMillis();

		System.out.println("--------------------mpkg1:"+mpkg1.toBytes().length);
		System.out.println("--------------------mska1:"+msk.a1.toBytes().length);
		System.out.println("--------------------mska1:"+msk.a2.toBytes().length);
		// Element t1 = mpk.g1.duplicate().powZn(x);
		Element t1 = mpkg1.powZn(x);
		times[3] = System.currentTimeMillis();
		Element t2 = mpkg1.powZn(r);
		times[4] = System.currentTimeMillis();
		Element t3 = sig.sig3.duplicate().powZn(r);// r*sig3
		times[5] = System.currentTimeMillis();

		sig.sig3.add(t1);
		times[6] = System.currentTimeMillis();
		sig.sig2.add(t2);
		times[7] = System.currentTimeMillis();

		Element t4 = sig.sig2.duplicate();
		System.out.println("dup time " + (System.currentTimeMillis() - times[7]));
		t4.powZn(x);// x*sig2'
		times[8] = System.currentTimeMillis();
		Element t5 = Hash(HashType.H3, (uid + msg));// Hash3((uid +
													// msg).getBytes());//H3(ID||msg)

		times[9] = System.currentTimeMillis();
		// Element t6 = isk.sk1.duplicate().powZn(t5);// H3(ID||msg)a1H1(ID)
		Element t6 = isksk1.powZn(t5);
		times[10] = System.currentTimeMillis();
		sig.sig1.add(t3);
		times[11] = System.currentTimeMillis();
		sig.sig1.add(t4);
		times[12] = System.currentTimeMillis();
		sig.sig1.add(isk.sk2);
		times[13] = System.currentTimeMillis();
		sig.sig1.add(t6);
		times[14] = System.currentTimeMillis();
		for (int i = 1; i < times.length; i++) {
			System.out.println("measure time " + i + ":" + (times[i] - times[i - 1]));
		}
		return sig.toBytes();
	}

	protected ISDSRPacket checkPacketFormat(ISDSRPacket pkt) {
		ByteBuffer bb = ByteBuffer.wrap(pkt.getSig());
		int length = 0;
		length = bb.getInt();
		byte[] sigbyte = new byte[((length + 4) * 3)];
		bb.position(bb.position() - 4);
		bb.get(sigbyte);
		pkt.setSig(sigbyte);
		return pkt;
	}

	@Override
	public boolean verify(ISDSRPacket pkt) {
		boolean ret = false;
		pkt = checkPacketFormat(pkt);
		System.out.println("start verification");
		System.out.println("packet\n" + pkt.toString());
		String[] uid = pkt.getRi().getAddrArray();
		String msg = "";

		IBSASSignature sig = generateSignatureFromBytes(pkt.getSig());
		int num = pkt.getRi().getRiLength();
		
		Element t1 = pairing.pairing(sig.sig1, mpk.g1);// e(sig1,g)
		Element t2 = pairing.pairing(sig.sig2, sig.sig3);// e(sig2,sig3)
		Element t3 = pairing.getG1().newElement().setToZero();
		Element t4, t5, t6;
		Element t7 = pairing.getG1().newElement().setToZero();
		Element t8, t9;

		String m1 = msg;
		for (int i = 0; i < num; i++) {
			t4 = Hash(HashType.H2, uid[i]);// Hash2(uid[i].getBytes());
			t3.add(t4);
			t5 = Hash(HashType.H1, uid[i]);// Hash1(uidByte);
			m1 = m1 + uid[i];
			t6 = Hash(HashType.H3, (uid[i] + m1));// Hash3(m2.getBytes());
			t5.powZn(t6);
			t7.add(t5);
		}
		t8 = pairing.pairing(t3, mpk.g3);
		t9 = pairing.pairing(t7, mpk.g2);
		t2.mul(t8);
		t2.mul(t9);

		if (t1.isEqual(t2)) {
			ret = true;
		} else {
			if (t1.invert().isEqual(t2)) {
				ret = true;
			}
		}
		System.out.println("result "+ret);
		return ret;
	}


	public IBSASSignature generateInitialSignature() {
		IBSASSignature sig = new IBSASSignature();
		sig.sig1 = pairing.getG1().newElement().setToOne();
		sig.sig2 = pairing.getG1().newElement().setToOne();
		sig.sig3 = pairing.getG1().newElement().setToOne();
		return sig;
	}

	public IBSASSignature generateSignatureFromBytes(byte[] bytes) {
		IBSASSignature sig = new IBSASSignature();
		if (bytes == null) {
			System.out.println("initiali signature");
			sig.sig1 = pairing.getG1().newElement().setToOne();
			sig.sig2 = pairing.getG1().newElement().setToOne();
			sig.sig3 = pairing.getG1().newElement().setToOne();
			return sig;
		}
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		int length = 0;
		byte[] sigbyte = null;
		for (int i = 0; i < 3; i++) {
			length = bb.getInt();
			sigbyte = new byte[length];
			bb.get(sigbyte);
			sig.setSig(i, generateG1ElementFromByte(sigbyte));
		}
		return sig;
	}

	public Element generateG1ElementFromByte(byte[] b) {
		return pairing.getG1().newElementFromBytes(b);
	}

	protected Element HashFromField(Field f, byte[] src) {
		return f.newElementFromHash(src, 0, src.length);
	}

	protected byte[] sha256Generator(byte[] src) {
		byte[] ret = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(src);
			ret = md.digest();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	protected Element Hash(HashType ht, String str) {

		byte[] src = null;
		Field f = null;
		switch (ht) {
		case H1: {
			src = str.getBytes();
			f = pairing.getG1();
			break;
		}
		case H2: {
			src = sha256Generator(str.getBytes());
			f = pairing.getG1();
			break;
		}
		case H3: {
			src = sha256Generator(str.getBytes());
			f = pairing.getZr();
			break;
		}
		}
		return HashFromField(f, src);
	}
}
