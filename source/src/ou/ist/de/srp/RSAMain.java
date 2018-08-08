package ou.ist.de.srp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import ou.ist.de.srp.node.Node;
import ou.ist.de.srp.node.RSANode;
import ou.ist.de.srp.packet.PacketManager;
import ou.ist.de.srp.packet.dsrbase.RSABasePacket;

public class RSAMain {
	
	public static void main(String[] args) {
		RSAMain m = new RSAMain();
		// m.generateKeys("a.properties", "keys_a.properties", 100);
		// m.generateKeys("a1.properties", "keys_a1.properties", 100);
		// m.generateKeys("e.properties", "keys_e.properties", 100);
		if (args.length != 1) {
			System.out.println("args\n" 
					+ "rsa:{512|1024|2048}:{s:dest ip|f} (without verification)\n"
					+ "rsa-v:{512|1024|2048}:{s:dest ip|f} (with verification)\n"
					+ "test-rsa:{512|1024|2048}:{s:dest ip|f}:nodes=value\n");
		}


		String[] argvalue = args[0].trim().split(":");
		if (argvalue[0].startsWith("rsa")) {
			//m.runRSA(argvalue);
			//m.runRSA(argvalue);
			m.runRSAMultihop(argvalue);
		}

	}

	public void testRSA(String[] args) {
		HashMap<String, String> params = new HashMap<String, String>();
		System.out.println("keysize=" + args[1]);
		params.put("keySize", args[1]);
		params.put("keyFile", "rsa" + args[1] + "_100keys.properties");
		params.put("index", "2");

		RSABasePacket pkt = genRSAPkt(3, params, "192.168.100.5");
		DatagramPacket dp;
		byte[] data;
		try {
			data = pkt.toByteArray();
			dp = new DatagramPacket(data, data.length, pkt.getDest(), Constants.PORT);

			RSANode rsanode = new RSANode(params);
			rsanode.setVerifyWhenForwarding(true);
			rsanode.getPacketManager().received(dp, rsanode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void runRSA(String[] args) {
		HashMap<String, String> params = new HashMap<String, String>();
		System.out.println("keysize=" + args[1]);
		params.put("keySize", args[1]);
		params.put("keyFile", "rsa" + args[1] + "_100keys.properties");
		params.put("index", "2");
		RSANode rsanode = new RSANode(params);
		if (args[0].split("-").length == 2) {
			rsanode.setVerifyWhenForwarding(true);
		}
		rsanode.startReception();

		if (args[2].equals("s")) {
			rsanode.startSearchRoute(args[3]);
		}
	}

	public RSABasePacket genRSAPkt(int nodes, HashMap<String, String> params, String dest) {
		PacketManager<RSABasePacket> pm = null;
		try {
			byte[][] tmp = new byte[nodes][4];
			tmp[0][0] = (byte) 1;
			tmp[0][1] = (byte) 1;
			tmp[0][2] = (byte) 1;
			tmp[0][3] = (byte) 1;
			RSANode[] fnodes = new RSANode[nodes];
			fnodes[0] = new RSANode(tmp[0], params);
			pm = fnodes[0].getPacketManager();
			RSABasePacket pkt = pm.generateInitialRequestPacket(fnodes[0], InetAddress.getByName(dest));
			for (int i = 1; i < nodes; i++) {
				for (int j = 0; j < tmp[i].length; j++) {
					tmp[i][j] = (byte) (i + 1);
				}
				fnodes[i] = new RSANode(tmp[i], params);
				pm = fnodes[i].getPacketManager();
				pkt = pm.generateForwardingPacket(pkt, fnodes[i]);
				System.out.println("pkt="+pkt.toString());

			}
			return pkt;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void runRSAMultihop(String[] args) {
		if(args[2].equals("s")) {

		HashMap<String, String> params = new HashMap<String, String>();
		System.out.println("keysize=" + args[1]);
		params.put("keySize", args[1]);
		params.put("keyFile", "rsa" + args[1] + "_100keys.properties");
		params.put("index", "2");
		RSANode rsanode = new RSANode(params);
		if (args[0].split("-").length == 3) {
			rsanode.setVerifyWhenForwarding(true);
		}

		Receiver rcv=new Receiver();
		rcv.node=rsanode;
		rcv.loop=true;
		Thread thread=new Thread(rcv);
		thread.start();
		DatagramPacket dp;
		byte[] data;
		int cnt=10;
		long waitTime=5000;
		try {
			rcv.hops=new int[cnt][2];
			rcv.hopsIndex=0;
			RSABasePacket pkt = rsanode.getPacketManager().generateInitialRequestPacket(rsanode, InetAddress.getByName(args[3]));
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
				rsanode.send(dp);
				Thread.sleep(3000);
			}
			int sum=0,avg,max=1,min=1;
			for(int i=0;i<cnt;i++) {
				System.out.println("hops["+i+"]="+rcv.hops[i]);
				sum+=rcv.hops[i][0];
				max=(max<rcv.hops[i][0])?rcv.hops[i][0]:max;
			}
			System.out.println("max="+max+" avg="+((double)sum)/cnt+"------");
			resultWrite(rcv.hops);
		}catch(Exception e) {
			e.printStackTrace();
		}
		}
		else {
			runRSA(args);
		}
		
	}
	public void runRSA(String[] args, int nodes) {
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
		double[][] rtt = new double[nodes / 10][cnt + 1];
		double total = 0;
		DatagramPacket dp;
		byte[] data;
		try {
			for (int i = 10; i <= nodes; i = i + 10) {
				RSABasePacket pkt = genRSAPkt(i, params, args[2].split("=")[1]);
				pkt = rsanode.getPacketManager().generateForwardingPacket(pkt, rsanode);
				data = pkt.toByteArray();
				dp = new DatagramPacket(data, data.length, pkt.getDest(), Constants.PORT);
				total = 0;
				rtt[i / 10 - 1][0] = i;
				for (int j = 1; j < rtt[i / 10 - 1].length; j++) {
					rsanode.send(dp);
					System.out.println("Constants wait=" + Constants.wait);
					while (true) {
						System.out.println(
								"Constants wait=" + Constants.wait + " i=" + i + " j=" + j + " total=" + total);
						if (!Constants.wait) {
							break;
						}
						// System.out.println("Constants wait="+Constants.wait);
					}
					rtt[i / 10 - 1][j] = ((double) Constants.timer / 1000);
					total += rtt[i / 10 - 1][j];
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String filename = args[0];
		for (int i = 1; i < args.length; i++) {
			filename += "_" + args[i];
		}
		Main.writeResult(filename + ".csv", rtt);
	}
	public static void resultWrite(int[][] data) {
		try {
			BufferedWriter bw =new BufferedWriter(new FileWriter("rsa-result.csv"));
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
		protected RSANode node;
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
						System.out.println("run -> send to "+forSend.getAddress()+" size "+forSend.getLength()+" hops["+hopsIndex+"]="+hops[hopsIndex][0]);
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
