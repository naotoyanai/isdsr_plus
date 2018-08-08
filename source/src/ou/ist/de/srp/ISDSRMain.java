package ou.ist.de.srp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import ou.ist.de.srp.node.ISDSRNode;
import ou.ist.de.srp.packet.PacketManager;
import ou.ist.de.srp.packet.dsrbase.ISDSRPacket;

public class ISDSRMain {
	
	public static void main(String[] args) {
		ISDSRMain m = new ISDSRMain();
		// m.generateKeys("a.properties", "keys_a.properties", 100);
		// m.generateKeys("a1.properties", "keys_a1.properties", 100);
		// m.generateKeys("e.properties", "keys_e.properties", 100);
		if (args.length != 1) {
			System.out.println("ibsas:{a|a1|e}:{s=dest ip|f} (without verification)\n" + 
					"ibsas-v:{a|a1|e}:{s=dest ip|f} (with verification)\n" + 
					"test-ibsas:{a|a1|e}:{s=dest ip}:nodes=value" + 
					"test-ibsas-v:{a|a1|e}:{s=dest ip}:nodes=value");
		}

		System.out.println("java.library.path=" + System.getProperty("java.library.path"));

		String[] argvalue = args[0].trim().split(":");
		if (argvalue[0].startsWith("ibsas")) {
			//m.runRSA(argvalue);
			//m.runISDSR(argvalue);
			m.testISDSR(argvalue);
			//m.runISDSRMultihop(argvalue);
		}
		/*
		 * String[] argvalue = args[0].trim().split(":");
		if (argvalue[0].startsWith("rsa")) {
			//m.runRSA(argvalue);
			m.runRSA(argvalue);
		}
		 * */

	}
	public void runISDSR(String[] args) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("paramFile", args[1] + ".properties");
		params.put("keyParamFile", "keys_" + args[1] + ".properties");
		params.put("index", "2");
		params.put("usePBC", "true");
		ISDSRNode rsanode = new ISDSRNode(params);
		if (args[0].endsWith("v")) {
			rsanode.setVerifyWhenForwarding(true);
		}
		rsanode.startReception();

		if (args[2].equals("s")) {
			rsanode.startSearchRoute(args[3]);
		}
	}
	public void testISDSR(String[] args) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("paramFile", args[1] + ".properties");
		params.put("keyParamFile", "keys_" + args[1] + ".properties");
		params.put("index", "2");
		params.put("usePBC", "true");

		ISDSRPacket pkt = genISDSRPkt(3, params, "192.168.100.5");
		DatagramPacket dp;
		byte[] data;
		try {
			data = pkt.toByteArray();
			dp = new DatagramPacket(data, data.length, pkt.getDest(), Constants.PORT);

			ISDSRNode rsanode = new ISDSRNode(params);
			rsanode.setVerifyWhenForwarding(true);
			rsanode.getPacketManager().received(dp, rsanode);
		} catch (Exception e) {
			e.printStackTrace();
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
		DatagramPacket dp;
		byte[] data;
		for (int i = div; i <= nodes; i=i+div) {
			ISDSRPacket pkt = genISDSRPkt(i, params, args[2].split("=")[1]);
			pkt = isdsrnode.getPacketManager().generateForwardingPacket(pkt, isdsrnode);
			data=pkt.toByteArray();
			dp=new DatagramPacket(data,data.length,pkt.getNext(),Constants.PORT);
			total=0;
			index=i/div-1;
			rtt[index][0]=i;
			for (int j = 1; j < rtt[index].length; j++) {
				isdsrnode.send(dp);
				System.out.println("Constants wait=" + Constants.wait);
				received=false;
				while (true) {
					System.out.println("Constants wait=" + Constants.wait + " i=" + i + " j="+j+" total=" + total);
					if (!Constants.wait) {
						received=true;
						break;
					}
					if((j>=1)&(System.currentTimeMillis()-Constants.timer)>(10000+(rtt[index][j-1]*1000))){
						isdsrnode.send(dp);
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

	public ISDSRPacket genISDSRPkt(int nodes, HashMap<String, String> params, String dest) {
		PacketManager<ISDSRPacket> pm = null;
		try {
			byte[][] tmp = new byte[nodes][4];
			tmp[0][0] = (byte) 1;
			tmp[0][1] = (byte) 1;
			tmp[0][2] = (byte) 1;
			tmp[0][3] = (byte) 1;
			ISDSRNode[] fnodes = new ISDSRNode[nodes];
			fnodes[0] = new ISDSRNode(tmp[0], params);
			pm = fnodes[0].getPacketManager();
			ISDSRPacket pkt = pm.generateInitialRequestPacket(fnodes[0], InetAddress.getByName(dest));
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
	public void runISDSRMultihop(String[] args) {
		if(args[2].equals("s")) {

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("paramFile", args[1] + ".properties");
			params.put("keyParamFile", "keys_" + args[1] + ".properties");
			params.put("index", "2");
			params.put("usePBC", "true");
		ISDSRNode node = new ISDSRNode(params);
		if (args[0].split("-").length == 3) {
			node.setVerifyWhenForwarding(true);
		}

		Receiver rcv=new Receiver();
		rcv.node=node;
		rcv.loop=true;
		Thread thread=new Thread(rcv);
		thread.start();
		DatagramPacket dp;
		byte[] data;
		int cnt=100;
		long waitTime=5000;
		try {
			rcv.hops=new int[cnt][2];
			rcv.hopsIndex=0;
			ISDSRPacket pkt = node.getPacketManager().generateInitialRequestPacket(node, InetAddress.getByName(args[3]));
			data = pkt.toByteArray();
			dp = new DatagramPacket(data, data.length, InetAddress.getByName(Constants.BROAD_CAST_ADDR), Constants.PORT);
			System.out.println(dp.getAddress());
			for(int i=0;i<cnt;i++) {
				while(true) {
					//System.out.println("(System.currentTimeMillis()-rcv.receivedTime)="+(System.currentTimeMillis()-rcv.receivedTime));

					if((System.currentTimeMillis()-rcv.receivedTime)>waitTime) {
						System.out.println("increment----------------------------------------------------------");
						break;
					}
				}
				rcv.hopsIndex++;
				node.send(dp);
				Thread.sleep(3000);
			}
			int sum=0,avg,max=1,min=1;
			for(int i=0;i<cnt;i++) {
				System.out.println("hops["+i+"][0]="+rcv.hops[i][0]);
				sum+=rcv.hops[i][0];
				max=(max<rcv.hops[i][0])?rcv.hops[i][0]:max;
			}
			resultWrite(rcv.hops);
			System.out.println("max="+max+" avg="+((double)sum)/cnt+"------");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		}
		else {
			runISDSR(args);
		}
		
	}
	public static void resultWrite(int[][] data) {
		try {
			BufferedWriter bw =new BufferedWriter(new FileWriter("isdsr-result.csv"));
			for(int i=0;i<data.length;i++) {
				bw.write(i+","+data[i][0]+","+data[i][1]+"\n");
			}
			bw.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	class Receiver implements Runnable {
		protected boolean loop;
		protected ISDSRNode node;
		protected long receivedTime;
		protected int[][] hops;
		protected int hopsIndex;
		
		public Receiver() {
			loop = false;
			receivedTime=0;
		}

		public void run() {
			try {
				DatagramPacket dp = new DatagramPacket(new byte[Constants.RCVBUFFER], Constants.RCVBUFFER);
				DatagramPacket forSend;
				DatagramSocket ds=node.getDs();
				InetAddress addr=node.getAddr();
				
				while (loop) {
					System.out.println("wait for packets");
					ds.receive(dp);
					receivedTime=System.currentTimeMillis();
					
					System.out.println("receive data");
					Constants.timer=System.currentTimeMillis()-Constants.timer;
					Constants.wait=false;
					System.out.println("received from "+dp.getAddress());
					if(dp.getAddress().getHostAddress().equals(addr.getHostAddress())) {
						System.out.println("received from self");
						continue;
					}
					forSend=node.getPacketManager().received(dp,node);
					if(forSend!=null) {
						System.out.println("run -> send to "+forSend.getAddress()+" size "+forSend.getLength()+" hops["+hopsIndex+"][0]="+hops[hopsIndex][0]);
						hops[hopsIndex][0]=node.getPacketManager().getNext().getHops();
						hops[hopsIndex][1]=forSend.getLength();
						node.send(forSend);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
