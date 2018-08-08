package ou.ist.de.srp.algo.ibsas_thres;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.ElementPowPreProcessing;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class IBSASThresholdalgorithm {

	protected Element g;
	protected ElementPowPreProcessing ppg;
	protected Pairing pairing;
	protected Element zero;
	protected Element one;

	protected ArrayList<DistributingData> aldd;
	protected int shares;
	protected int total;
	protected HashMap<Integer, ReceivedShares> rcv;
	protected MPK mpk;
	protected ISK isk;
	protected Element a1i;
	protected Element a2i;
	protected int id;

	public static long time1, time2, time3, time4;

	enum HashType {
		H1, H2, H3
	};

	public IBSASThresholdalgorithm() {
		//System.out.println("ThresholdGDH algorithm constructor");

		PairingFactory.getInstance().setUsePBCWhenPossible(true);
		setPairing("a.properties");

		System.out.println("use pbc:" + PairingFactory.getInstance().isPBCAvailable());
		initialize();
	}

	public IBSASThresholdalgorithm(HashMap<String, String> params) {
		// super(params);
		String paramFile = params.get("paramFile");
		String keyParamFile = params.get("keyParamFile");
		String index = params.get("index");
		String usePBC = params.get("usePBC");
		String uid = params.get("uid");
		String total = params.get("total");
		String shares = params.get("shares");

		this.total = Integer.valueOf(total);
		this.shares = Integer.valueOf(shares);

		//System.out.println("ThresholdGDH algorithm constructor");

		if (usePBC != null && !usePBC.equalsIgnoreCase("false")) {

			PairingFactory.getInstance().setUsePBCWhenPossible(true);
		}
		if (paramFile != null) {
			setPairing(paramFile);

			System.out.println("use pbc:" + PairingFactory.getInstance().isPBCAvailable());

		} else {
			System.out.println("No parameters were selected.");
			System.exit(0);
		}
		if (keyParamFile != null && index != null) {
			setKeys(keyParamFile, index);
		}
		initialize();
	}

	protected void setKeys(String keyParamFile, String indexstr) {
		try {
			int index = Integer.valueOf(indexstr);
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyParamFile));
			ArrayList<byte[]> ale = (ArrayList<byte[]>) ois.readObject();
			ois.close();
			g = pairing.getG1().newElementFromBytes(ale.get(index));
			ppg=g.getElementPowPreProcessing();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void initialize() {
		zero = pairing.getZr().newElement().setToZero();
		one = pairing.getZr().newElement().setToOne();
		rcv = new HashMap<Integer, ReceivedShares>();
		mpk = new MPK();
		isk = new ISK();
	}

	protected void setPairing(String paramFile) {
		pairing = PairingFactory.getPairing(paramFile);
	}
	

	public ArrayList<DistributingData> generateDistributingData() {
		aldd = new ArrayList<DistributingData>();
		DistributingShares ds = new DistributingShares();

		long time = System.currentTimeMillis();
		ds.generateDistributingShares(total, shares, pairing);
		System.out.println("distributing share generation:" + (System.currentTimeMillis() - time));

		time = System.currentTimeMillis();
		DistributingData dd = new DistributingData(id, -1, ShareType.A10G, g.duplicate().powZn(ds.a10));
		aldd.add(dd);
		dd = new DistributingData(id, -1, ShareType.A20G, g.duplicate().powZn(ds.a20));
		aldd.add(dd);
		System.out.println("distributing a1i0 a2i0 generation:" + (System.currentTimeMillis() - time));

		for (int i = 1; i < ds.a1ij.length; i++) {
			dd = new DistributingData(id, i, ShareType.A1ij, ds.a1ij[i]);
			aldd.add(dd);
			dd = new DistributingData(id, i, ShareType.A2ij, ds.a2ij[i]);
			aldd.add(dd);
		}
		return aldd;
	}

	protected void receiveShares(DistributingData dd) {
		ReceivedShares rs;
		 //System.out.println("id="+id+" from:"+dd.from+" to:"+dd.to+" type="+dd.type);
		if (rcv.containsKey(dd.from)) {
			rs = rcv.get(dd.from);

		} else {
			rs = new ReceivedShares();
		}
		switch (dd.type) {
		case ShareType.A10G: {
			rs.a1i0g = dd.e;
			break;
		}
		case ShareType.A20G: {
			rs.a2i0g = dd.e;
			break;
		}
		}
		if (dd.to == id) {
			// System.out.println("1 id="+id+" from:"+dd.from+" to:"+dd.to+"
			// type="+dd.type);
			switch (dd.type) {
			case ShareType.A1ij: {
				rs.a1ij = dd.e;
				break;
			}
			case ShareType.A2ij: {
				rs.a2ij = dd.e;
				break;
			}
			case ShareType.A1jH: {
				rs.a1jH = dd.e;
				break;
			}
			case ShareType.A2jH: {
				rs.a2jH = dd.e;
				break;
			}
			}
		}
		rcv.put(dd.from, rs);
	}

	protected boolean finishCollectingShares(int type) {
		int cnt = 0;
		Element e = null;
		ReceivedShares rs = null;
		for (Integer key : rcv.keySet()) {
			rs = rcv.get(key);
			switch (type) {
			case ShareType.A10G: {
				e = rs.a1i0g;
				break;
			}
			case ShareType.A20G: {
				e = rs.a2i0g;
				break;
			}
			case ShareType.A1ij: {
				e = rs.a1ij;
				break;
			}
			case ShareType.A2ij: {
				e = rs.a2ij;
				break;
			}
			case ShareType.A1jH: {
				e = rs.a1jH;
				break;
			}
			case ShareType.A2jH: {
				e = rs.a2jH;
				break;
			}
			}
			if (e != null) {
				cnt++;
			}
		}
		System.out.println("id:" + id + "----------------------cnt " + cnt);
		return cnt >= shares;
	}

	protected void recoverPrivateKey() {
		Element[] a1jh = new Element[shares];
		Element[] a2jh = new Element[shares];
		int[] indices1 = new int[shares];
		int[] indices2 = new int[shares];
		int cnt1 = 0, cnt2 = 0;
		ReceivedShares rs = null;
		for (Integer key : rcv.keySet()) {
			rs = rcv.get(key);
			if (cnt1 < shares) {
				if (rs.a1jH != null) {

					//System.out.println(" cnt 1");
					a1jh[cnt1] = rs.a1jH;
					cnt1++;
					indices1[cnt1 - 1] = cnt1;
				}
			}
			if (cnt2 < shares) {
				if (rs.a2jH != null) {
					//System.out.println(" cnt 2");
					a2jh[cnt2] = rs.a2jH;
					cnt2++;
					indices2[cnt2 - 1] = cnt2;
				}
			}
			if (cnt1 >= shares && cnt2 >= shares) {
				break;
			}
		}
		long time = System.currentTimeMillis();
		Element tmpa1g = pairing.getG1().newElementFromBytes(a1jh[0].toBytes());
		Element tmpa2g = pairing.getG1().newElementFromBytes(a2jh[0].toBytes());
		Element a1g = tmpa1g.powZn(lagrange(indices1[0], indices1, indices1.length));
		Element a2g = tmpa2g.powZn(lagrange(indices2[0], indices2, indices2.length));

		for (int i = 1; i < indices1.length; i++) {
			tmpa1g = pairing.getG1().newElement().set(a1jh[i]);
			tmpa2g = pairing.getG1().newElement().set(a2jh[i]);
			a1g = a1g.mul(tmpa1g.powZn(lagrange(indices1[i], indices1, indices1.length)));
			a2g = a2g.mul(tmpa2g.powZn(lagrange(indices2[i], indices2, indices2.length)));
			//System.out.println(i + "a1g:" + javax.xml.bind.DatatypeConverter.printHexBinary(a1g.toBytes()));
			//System.out.println(i + "a2g:" + javax.xml.bind.DatatypeConverter.printHexBinary(a2g.toBytes()));
		}
		isk.sk1 = a1g;
		isk.sk2 = a2g;
		System.out.println("recover private key:" + (System.currentTimeMillis() - time));

		// System.out.println("sk1:" +
		// javax.xml.bind.DatatypeConverter.printHexBinary(isk.sk1.toBytes()));
		// System.out.println("sk2:" +
		// javax.xml.bind.DatatypeConverter.printHexBinary(isk.sk2.toBytes()));
	}
	public DistributingData requestPrivateKeyShare(int id,String ID,int type) {
		DistributingData ret=null;
		Element e;
		Element share;
		
		switch(type){
			case ShareType.A1jH:{
				e=Hash(HashType.H1, ID);
				share=a1i;
				break;
			}
			case ShareType.A2jH:{
				e=Hash(HashType.H2, ID);
				share=a2i;
				break;
			}
			default:{
				return null;
			}
		}
		ret=new DistributingData(this.id,id,type,e.powZn(share));
		return ret;
	}
	protected ArrayList<DistributingData> recoverPrivateKeyShare() {
		a1i = pairing.getZr().newZeroElement();
		a2i = pairing.getZr().newZeroElement();

		Element[] a1ij = new Element[total];
		Element[] a2ij = new Element[total];
		int[] indices1 = new int[total];
		int[] indices2 = new int[total];
		int cnt1 = 0, cnt2 = 0;
		ReceivedShares rs = null;
		//System.out.println("recived shares"+rcv.size());
		for (Integer key : rcv.keySet()) {
			
			rs = rcv.get(key);
			if (cnt1 < total) {
				if (rs.a1ij != null) {
					//System.out.println("a1ij="+a1ij);
					a1ij[cnt1] = rs.a1ij;
					cnt1++;
					indices1[cnt1 - 1] = key;
				}
			}
			if (cnt2 < total) {
				if (rs.a2ij != null) {
					a2ij[cnt2] = rs.a2ij;
					cnt2++;
					indices2[cnt2 - 1] = key;
				}
			}
			if (cnt1 >= total && cnt2 >= total) {
				break;
			}
		}
		long time = System.currentTimeMillis();
		for (int i = 0; i < indices1.length; i++) {
			//System.out.println("id="+id+" index="+i+" a1i="+a1i+" a1ij="+a1ij[i]);
			a1i = a1i.add(a1ij[i]);
			a2i = a2i.add(a2ij[i]);
		}
		System.out.println("recover private key share:" + (System.currentTimeMillis() - time));
		/*
		Element h1;
		Element h2;
		DistributingData dd = null;
		aldd = new ArrayList<DistributingData>();
		time=System.currentTimeMillis();
		for (int i = 0; i < a1ij.length; i++) {
			h1 = Hash(HashType.H1, String.valueOf(indices1[i]));
			h2 = Hash(HashType.H2, String.valueOf(indices2[i]));
			// System.out.println("indices1["+i+"]="+indices1[i]);
			h1.powZn(a1i);
			h2.powZn(a2i);
			dd = new DistributingData(id, indices1[i], ShareType.A1jH, h1);
			aldd.add(dd);
			dd = new DistributingData(id, indices2[i], ShareType.A2jH, h2);
			aldd.add(dd);
		}
		System.out.println("recover private key Hashed share:"+(System.currentTimeMillis()-time));
		*/
		return aldd;

	}

	protected void recoverMasterPublicKeys() {
		Element[] a10gs = new Element[shares];
		Element[] a20gs = new Element[shares];
		int[] indices1 = new int[shares];
		int[] indices2 = new int[shares];
		int cnt1 = 0, cnt2 = 0;
		ReceivedShares rs = null;
		for (Integer key : rcv.keySet()) {
			rs = rcv.get(key);
			if (cnt1 < shares) {
				if (rs.a1i0g != null) {
					a10gs[cnt1] = rs.a1i0g;
					cnt1++;
					indices1[cnt1 - 1] = cnt1;
				}
			}
			if (cnt2 < shares) {
				if (rs.a2i0g != null) {
					a20gs[cnt2] = rs.a2i0g;
					cnt2++;
					indices2[cnt2 - 1] = cnt2;
				}
			}
			if (cnt1 >= shares && cnt2 >= shares) {
				break;
			}
		}
		long time = System.currentTimeMillis();
		Element tmpa1g = pairing.getG1().newElementFromBytes(a10gs[0].toBytes());
		Element tmpa2g = pairing.getG1().newElementFromBytes(a20gs[0].toBytes());
		Element a1g = tmpa1g.powZn(lagrange(indices1[0], indices1, indices1.length));
		Element a2g = tmpa2g.powZn(lagrange(indices2[0], indices2, indices2.length));

		for (int i = 0; i < indices1.length; i++) {
			tmpa1g = pairing.getG1().newElement().set(a10gs[i]);
			tmpa2g = pairing.getG1().newElement().set(a20gs[i]);
			a1g = a1g.mul(tmpa1g.powZn(lagrange(indices1[i], indices1, indices1.length)));
			a2g = a2g.mul(tmpa2g.powZn(lagrange(indices2[i], indices2, indices2.length)));
			// System.out.println(i + " a1g:" +
			// javax.xml.bind.DatatypeConverter.printHexBinary(a1g.toBytes()));
			// System.out.println(i + " a2g:" +
			// javax.xml.bind.DatatypeConverter.printHexBinary(a2g.toBytes()));
		}
		mpk.g2 = a1g;
		mpk.g3 = a2g;
		System.out.println("recover master pub key:" + (System.currentTimeMillis() - time));
	}

	public Element polynomialEvaluation(int x, int size, Element[] a) {
		Element y = pairing.getZr().newElement().setToZero();
		Element xx = pairing.getZr().newElement().setToOne();
		Element tmpa;
		for (int i = 0; i < size; i++) {
			Element xval = pairing.getZr().newElement().set(x);
			tmpa = a[i].duplicate().mul(xx);
			y = y.add(tmpa);
			xx = xx.mul(xval);
		}
		return y;
	}

	public Element lagrange(int j, int[] a, int l) {

		Element num = pairing.getZr().newElement().setToOne();
		Element den = pairing.getZr().newElement().setToOne();
		Element jj = pairing.getZr().newElement().set(j);
		Element ii;
		Element tmp1 = null, tmp2 = null;
		for (int i = 0; i < l; i++) {
			ii = pairing.getZr().newElement().set(a[i]);
			if (!ii.isEqual(jj)) {
				tmp1 = zero.duplicate().sub(ii).sub(one);
				num = num.mul(tmp1);
				tmp2 = jj.duplicate().sub(ii);
				den = den.mul(tmp2);
			}
		}

		return num.div(den);
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

	public static void main(String[] args) {
		int t=0,p=0;
		if(args.length>1) {
			if(args[0].startsWith("n=")) {
				t=Integer.valueOf(args[0].split("=")[1]);
			}
			if(args[1].startsWith("p=")) {
				p=Integer.valueOf(args[1].split("=")[1]);
			}
			runTest(t,p);
		}
		IBSASThresholdalgorithm.readResult(new String[] {"results-cn.txt","results-cn2.txt"});
		//runTest();
	}
	public static void runTest() {
		long time = 0;
		String paramPrefix = "a";
		int[] para1 = new int[] { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
		int[] para2 = new int[] { 5, 10, 15, 20, 25, 30 };
		String total = "100";
		String shares = "50";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("paramFile", paramPrefix + ".properties");
		params.put("keyParamFile", "keys_" + paramPrefix + "G.properties");
		params.put("index", "2");
		params.put("usePBC", "true");
		params.put("total", total);
		params.put("shares", shares);
		ArrayList<DistributingData> aldd;
		IBSASThresholdalgorithm[] nodes;
		int t=60, p;
		for ( ; t <=100; t = t + 10) {
			for ( p=5; p <= 50 && p<t; p = p + 5) {
				//p = pcnt + 5;
				System.out.println("parameters:N=" + t + ":P=" + p);
				nodes = new IBSASThresholdalgorithm[t];
				for (int i = 0; i < t; i++) {
					nodes[i] = new IBSASThresholdalgorithm(params);
					nodes[i].total = t;
					nodes[i].shares = p;
					nodes[i].id = i + 1;
				}
				for (int i = 0; i < nodes.length; i++) {
					nodes[i].generateDistributingData();
				}
				for (int i = 0; i < nodes.length; i++) {
					for (int j = 0; j < nodes[i].aldd.size(); j++) {
						for (int k = 0; k < nodes.length; k++) {
							nodes[k].receiveShares(nodes[i].aldd.get(j));
						}
					}
				}
				for(int i=0;i<t;i++) {
					nodes[i].recoverMasterPublicKeys();
					nodes[i].recoverPrivateKeyShare();
				}
				time=System.currentTimeMillis();
				for(int i=0;i<p;i++) {
					nodes[0].receiveShares(nodes[i+1].requestPrivateKeyShare(nodes[0].id, String.valueOf(nodes[0].id), ShareType.A1jH));
					nodes[0].receiveShares(nodes[i+1].requestPrivateKeyShare(nodes[0].id, String.valueOf(nodes[0].id), ShareType.A2jH));
				}
				System.out.println("request isk shares:"+(System.currentTimeMillis()-time));
				for(int i=0;i<100;i++) {
					nodes[0].recoverPrivateKey();
					System.out.println("recover -----------------------");
				}
			}
		}
	}
	public static void runTest(int t,int p) {
		String paramPrefix = "a";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("paramFile", paramPrefix + ".properties");
		params.put("keyParamFile", "keys_" + paramPrefix + "G.properties");
		params.put("index", "2");
		params.put("usePBC", "true");
		params.put("total", ""+t);
		params.put("shares", ""+p);
		ArrayList<DistributingData> aldd;
		IBSASThresholdalgorithm[] nodes;
		System.out.println("parameters:N=" + t + ":P=" + p);
		nodes = new IBSASThresholdalgorithm[t];
		long time;
		for (int i = 0; i < t; i++) {
			nodes[i] = new IBSASThresholdalgorithm(params);
			nodes[i].total = t;
			nodes[i].shares = p;
			nodes[i].id = i + 1;
		}
		for (int i = 0; i < nodes.length; i++) {
			nodes[i].generateDistributingData();
		}
		for (int i = 0; i < nodes.length; i++) {
			for (int j = 0; j < nodes[i].aldd.size(); j++) {
				for (int k = 0; k < nodes.length; k++) {
					nodes[k].receiveShares(nodes[i].aldd.get(j));
				}
			}
		}
		for(int i=0;i<t;i++) {
			nodes[i].recoverMasterPublicKeys();
			nodes[i].recoverPrivateKeyShare();
		}
		time=System.currentTimeMillis();
		for(int i=0;i<p;i++) {
			nodes[0].receiveShares(nodes[i+1].requestPrivateKeyShare(nodes[0].id, String.valueOf(nodes[0].id), ShareType.A1jH));
			nodes[0].receiveShares(nodes[i+1].requestPrivateKeyShare(nodes[0].id, String.valueOf(nodes[0].id), ShareType.A2jH));
		}
		System.out.println("request isk shares:"+(System.currentTimeMillis()-time));
		for(int i=0;i<10;i++) {
			nodes[0].recoverPrivateKey();
			System.out.println("recover -----------------------");
		}
	}
	public static void readResult(String[] files) {
		BufferedReader br;
		BufferedWriter bw;
		String line;

		try {
			ResultData rd;
			int[][] r=null;
			double[] avg=new double[6];
			String[] title=new String[] {
					"a1ij a2ij generation",
					"a1i0g  a2i0g generation",
					"recover mpk",
					"recover pk share",
					"a1ijH  a2ijH generation",
					"recover isk"
			};
			bw=new BufferedWriter(new FileWriter("results-compnet.csv"));
			for(int i=0;i<title.length;i++) {
				bw.write(","+title[i]);
			}
			bw.write("\n");
			for(String f:files) {
				br=new BufferedReader(new FileReader(f));
				r=null;
				System.out.println("file "+f);
				while((line=br.readLine())!=null) {
					if(line.startsWith("parameters")) {
						if(r==null) {
							r=new int[7][2];
						}
						else {
							System.out.println("N="+r[0][0]+" P="+r[0][1]);
							bw.write("N="+r[0][0]+" P="+r[0][1]);
							for(int i=1;i<r.length;i++) {
								avg[i-1]=((double)r[i][1])/r[i][0];
								bw.write(","+avg[i-1]);
								System.out.println(title[i-1]+":"+avg[i-1]);
							}
							bw.write("\n");
						}
						for(int i=0;i<r.length;i++) {
							r[i][0]=0;
							r[i][1]=0;
						}
						r[0][0]=Integer.valueOf(line.split(":")[1].split("=")[1]);
						r[0][1]=Integer.valueOf(line.split(":")[2].split("=")[1]);
					}
					if(line.startsWith("distributing share")) {
						r[1][0]+=1;
						r[1][1]+=Integer.valueOf(line.split(":")[1]);
						
					}
					if(line.startsWith("distributing a1i0")) {
						r[2][0]+=1;
						r[2][1]+=Integer.valueOf(line.split(":")[1]);
						
					}
					if(line.startsWith("recover master")) {
						r[3][0]+=1;
						r[3][1]+=Integer.valueOf(line.split(":")[1]);
						
					}
					if(line.startsWith("recover private key share")){
						r[4][0]+=1;
						r[4][1]+=Integer.valueOf(line.split(":")[1]);
						
					}
					if(line.startsWith("request isk")) {
						r[5][0]+=1;
						r[5][1]+=Integer.valueOf(line.split(":")[1]);
						
					}
					if(line.startsWith("recover private key:")) {
						r[6][0]+=1;
						r[6][1]+=Integer.valueOf(line.split(":")[1]);
						
					}
					
				}
				System.out.println("N="+r[0][0]+" P="+r[0][1]);
				bw.write("N="+r[0][0]+" P="+r[0][1]);
				for(int i=1;i<r.length;i++) {
					avg[i-1]=((double)r[i][1])/r[i][0];
					bw.write(","+avg[i-1]);
					System.out.println(title[i-1]+":"+avg[i-1]);
				}
				bw.write("\n");
				br.close();
			}
			bw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	class ResultData{
		int p;
		int k;
		int sg;
		int gsg;
		int mpk;
		int pks;
		int pkh;
		int pk;
	}
}
