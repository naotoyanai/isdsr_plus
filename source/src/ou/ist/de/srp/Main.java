package ou.ist.de.srp;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.HashMap;

import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import ou.ist.de.srp.algo.ibsas.MasterKey;
import ou.ist.de.srp.node.ISDSRNode;
import ou.ist.de.srp.packet.Packet;
import ou.ist.de.srp.packet.PacketManager;
import ou.ist.de.srp.node.RSANode;

public class Main {
	public static void main(String args[]) {

		Main m = new Main();
		// m.generateKeys("a.properties", "keys_a.properties", 100);
		// m.generateKeys("a1.properties", "keys_a1.properties", 100);
		// m.generateKeys("e.properties", "keys_e.properties", 100);
		if (args.length != 1) {
			System.out.println("args\n" + 
					"ibsas:{a|a1|e}:{s=dest ip|f} (without verification)\n" + 
					"ibsas-v:{a|a1|e}:{s=dest ip|f} (with verification)\n" + 
					"rsa:{512|1024|2048}:{s:dest ip|f} (without verification)\n"+
					"rsa-v:{512|1024|2048}:{s:dest ip|f} (with verification)\n"+
					"test-rsa:{512|1024|2048}:{s:dest ip|f}:nodes=value\n"+
					"test-ibsas:{a|a1|e}:{s=dest ip}:nodes=value" + 
					"test-ibsas-v:{a|a1|e}:{s=dest ip}:nodes=value");
		}
		System.out.println("java.library.path="+System.getProperty("java.library.path"));
		String[] argvalue = args[0].trim().split(":");
		if (argvalue[0].startsWith("ibsas")) {
			m.runISDSR(argvalue);
		} else if (argvalue[0].startsWith("rsa")) {
			m.runRSA(argvalue);
		} else if (argvalue[0].startsWith("test-ibsas")) {
			m.runISDSR(argvalue, Integer.valueOf(argvalue[3].split("=")[1]));
		}
		else if(argvalue[0].startsWith("test-rsa")){
			m.runRSA(argvalue, Integer.valueOf(argvalue[3].split("=")[1]));
		}

	}

	public void runISDSR(String[] args) {
		HashMap<String, String> params = new HashMap<String, String>();

		params.put("paramFile", args[1] + ".properties");
		params.put("keyParamFile", "keys_" + args[1] + ".properties");
		params.put("index", "2");
		params.put("usePBC", "true");
		// ISDSRNode isdsrnode=new ISDSRNode(new byte[]{1,2,3,4},params);
		ISDSRNode isdsrnode = new ISDSRNode(params);
		if (args[0].split("-").length == 2) {
			isdsrnode.setVerifyWhenForwarding(true);
		}
		isdsrnode.startReception();
		if (args[2].startsWith("s")) {
			System.out.println("startswith s "+args[2]);
			isdsrnode.startSearchRoute(args[2].split("=")[1]);
		}

	}

	public void runISDSR(String[] args, boolean pbc) {
		HashMap<String, String> params = new HashMap<String, String>();

		params.put("paramFile", args[1] + ".properties");
		params.put("keyParamFile", "keys_" + args[1] + ".properties");
		params.put("index", "2");
		params.put("usePBC", (pbc ? "true" : "false"));
		// ISDSRNode isdsrnode=new ISDSRNode(new byte[]{1,2,3,4},params);
		ISDSRNode isdsrnode = new ISDSRNode(params);
		isdsrnode.startReception();
		if (args[2].startsWith("s")) {
			isdsrnode.startSearchRoute(args[2].split("=")[1]);
		}
	}

	public void runISDSR(String[] args, int nodes) {
		HashMap<String, String> params = new HashMap<String, String>();

		params.put("paramFile", args[1] + ".properties");
		params.put("keyParamFile", "keys_" + args[1] + ".properties");
		params.put("index", "2");
		params.put("usePBC", "true");
		// ISDSRNode isdsrnode=new ISDSRNode(new byte[]{1,2,3,4},params);
		ISDSRNode isdsrnode = new ISDSRNode(params);
		if (args[0].split("-").length == 3) {
			isdsrnode.setVerifyWhenForwarding(true);
		}
		isdsrnode.startReception();
		int div=10;
		int cnt = 100;
		int index=0;
		double[][] rtt = new double[nodes/div][cnt+1];
		double total = 0;
		boolean received=false;
		long waiting=0;
		for (int i = div; i <= nodes; i=i+div) {
			Packet pkt = genISDSRPkt(i, params, args[2].split("=")[1]);
			pkt = isdsrnode.getPacketManager().generateForwardingPacket(pkt, isdsrnode);
			total=0;
			index=i/div-1;
			rtt[index][0]=i;
			for (int j = 1; j < rtt[index].length; j++) {
				isdsrnode.send(pkt.getDest(), pkt, Constants.PORT);
				System.out.println("Constants wait=" + Constants.wait);
				received=false;
				while (true) {
					System.out.println("Constants wait=" + Constants.wait + " i=" + i + " j="+j+" total=" + total);
					if (!Constants.wait) {
						received=true;
						break;
					}
					if((j>=1)&(System.currentTimeMillis()-Constants.timer)>(10000+(rtt[index][j-1]*1000))){
						isdsrnode.send(pkt.getDest(), pkt, Constants.PORT);
						continue;
					}
					// System.out.println("Constants wait="+Constants.wait);
				}
				if(received) {
				rtt[index][j] = ((double)Constants.timer/1000);
				total += rtt[index][j];
				}
			}
		}
		String filename=args[0];
		for(int i=1;i<args.length;i++) {
			filename+="_"+args[i];
		}
		Main.writeResult(filename+".csv",rtt);
	}

	public Packet genISDSRPkt(int nodes, HashMap<String, String> params, String dest) {
		PacketManager pm = null;
		try {
			byte[][] tmp = new byte[nodes][4];
			tmp[0][0] = (byte) 1;
			tmp[0][1] = (byte) 1;
			tmp[0][2] = (byte) 1;
			tmp[0][3] = (byte) 1;
			ISDSRNode[] fnodes = new ISDSRNode[nodes];
			fnodes[0] = new ISDSRNode(tmp[0], params);
			pm = fnodes[0].getPacketManager();
			Packet pkt = pm.generateInitialRequestPacket(fnodes[0], InetAddress.getByName(dest));
			for (int i = 1; i < nodes; i++) {
				for (int j = 0; j < tmp[i].length; j++) {
					tmp[i][j] = (byte) (i + 1);
				}
				fnodes[i] = new ISDSRNode(tmp[i], params);
				pm = fnodes[i].getPacketManager();
				pkt = pm.generateForwardingPacket(pkt, fnodes[i]);

			}
			return pkt;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void runRSA(String[] args) {
		HashMap<String, String> params = new HashMap<String, String>();
		System.out.println("keysize=" + args[1]);
		params.put("keySize", args[1]);
		params.put("keyFile", "rsa" + args[1] + "_100keys.properties");
		params.put("index", "2");
		RSANode rsanode = new RSANode(params);
		if (args[0].split("-").length == 3) {
			rsanode.setVerifyWhenForwarding(true);
		}
		rsanode.startReception();
		
		if (args[2].equals("s")) {
			rsanode.startSearchRoute(args[3]);
		}
	}
	
	public void runRSA(String[] args,int nodes) {
		HashMap<String, String> params = new HashMap<String, String>();
		System.out.println("keysize=" + args[1]);
		params.put("keySize", args[1]);
		params.put("keyFile", "rsa" + args[1] + "_100keys.properties");
		params.put("index", "2");
		RSANode rsanode = new RSANode(params);
		if (args[0].split("-").length == 3) {
			rsanode.setVerifyWhenForwarding(true);
		}
		rsanode.startReception();
		int cnt = 100;
		double[][] rtt = new double[nodes/10][cnt+1];
		double total = 0;
		for (int i = 10; i <= nodes; i=i+10) {
			Packet pkt = genRSAPkt(i, params, args[2].split("=")[1]);
			pkt = rsanode.getPacketManager().generateForwardingPacket(pkt, rsanode);
			total=0;
			rtt[i/10-1][0]=i;
			for (int j = 1; j < rtt[i/10-1].length; j++) {
				rsanode.send(pkt.getDest(), pkt, Constants.PORT);
				System.out.println("Constants wait=" + Constants.wait);
				while (true) {
					System.out.println("Constants wait=" + Constants.wait + " i=" + i + " j="+j+" total=" + total);
					if (!Constants.wait) {
						break;
					}
					// System.out.println("Constants wait="+Constants.wait);
				}
				rtt[i/10-1][j] = ((double)Constants.timer/1000);
				total += rtt[i/10-1][j];
			}
		}
		String filename=args[0];
		for(int i=1;i<args.length;i++) {
			filename+="_"+args[i];
		}
		Main.writeResult(filename+".csv",rtt);
	}
	public Packet genRSAPkt(int nodes, HashMap<String, String> params, String dest) {
		PacketManager pm = null;
		try {
			byte[][] tmp = new byte[nodes][4];
			tmp[0][0] = (byte) 1;
			tmp[0][1] = (byte) 1;
			tmp[0][2] = (byte) 1;
			tmp[0][3] = (byte) 1;
			RSANode[] fnodes = new RSANode[nodes];
			fnodes[0] = new RSANode(tmp[0], params);
			pm = fnodes[0].getPacketManager();
			Packet pkt = pm.generateInitialRequestPacket(fnodes[0], InetAddress.getByName(dest));
			for (int i = 1; i < nodes; i++) {
				for (int j = 0; j < tmp[i].length; j++) {
					tmp[i][j] = (byte) (i + 1);
				}
				fnodes[i] = new RSANode(tmp[i], params);
				pm = fnodes[i].getPacketManager();
				pkt = pm.generateForwardingPacket(pkt, fnodes[i]);

			}
			return pkt;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void writeResult(String file,double[][] values) {
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File(file)));
			for(int i=0;i<values.length;i++) {
				for(int j=0;j<values[i].length;j++) {
					bw.write(String.valueOf(values[i][j]+","));
				}
				bw.write("\n");
			}
			bw.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void generateKeys(String paramFile, String outFile, int repeat) {
		Pairing pairing = PairingFactory.getPairing(paramFile);

		MasterKey mk = new MasterKey();
		mk.paramFile = paramFile;
		mk.ale = new MasterKey.Elements[repeat];
		for (int i = 0; i < repeat; i++) {
			mk.ale[i] = mk.new Elements();
			mk.ale[i].g = pairing.getG1().newRandomElement().toBytes();
			mk.ale[i].a1 = pairing.getZr().newRandomElement().toBytes();
			mk.ale[i].a2 = pairing.getZr().newRandomElement().toBytes();
		}
		try {
			FileOutputStream fos = new FileOutputStream(outFile);
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(fos));
			oos.writeObject(mk);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
