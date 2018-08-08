package ou.ist.de.srp.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import ou.ist.de.srp.Constants;
import ou.ist.de.srp.algo.AbstractAlgorithm;
import ou.ist.de.srp.packet.Packet;
import ou.ist.de.srp.packet.PacketManager;

public abstract class Node<T extends Packet> {
	protected String id;
	protected InetAddress addr;
	protected PacketManager<T> pktmgr;
	protected DatagramSocket ds;
	protected Receiver rcv;
	protected boolean verifyWhenForwarding;
	protected int seqnum;
	protected int port;

	public Node(byte[] addr, HashMap<String, String> params) {
		try {
			seqnum=0;
			ds=null;
			this.verifyWhenForwarding=false;
			this.addr = InetAddress.getByAddress(addr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		pktmgr=initializePacketManager(params);
		pktmgr.setAlgorithm(generateSignatureAlgorithm(params));
	}
	public Node(HashMap<String, String> params,int port) {
		seqnum=0;
		this.verifyWhenForwarding=false;
		this.port=port;
		initializeAddress();
		initializeDatagramSocket();
		rcv = new Receiver();
		rcv.node=this;
		pktmgr=initializePacketManager(params);
		pktmgr.setAlgorithm(generateSignatureAlgorithm(params));
	}
	public Node(HashMap<String, String> params) {
		seqnum=0;
		this.verifyWhenForwarding=false;
		this.port=Constants.PORT;
		initializeAddress();
		initializeDatagramSocket();
		rcv = new Receiver();
		rcv.node=this;
		pktmgr=initializePacketManager(params);
		pktmgr.setAlgorithm(generateSignatureAlgorithm(params));
	}
	public void setVerifyWhenForwarding(boolean v) {
		this.verifyWhenForwarding=v;
		pktmgr.setVerification(v);
	}
	protected abstract PacketManager<T> initializePacketManager(HashMap<String, String> params);
	protected abstract AbstractAlgorithm<T> generateSignatureAlgorithm(HashMap<String, String> params);

	protected void initializeDatagramSocket() {
		try {
			//dsR = new DatagramSocket(this.port);
			System.out.println("initialize datagram socket");
			//ds=new DatagramSocket(port,addr);
			ds=new DatagramSocket(port);
			ds.setBroadcast(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void initializeAddress() {
		ProcessBuilder pb = new ProcessBuilder("ip", "-f", "inet", "a");

		try {
			Process p = pb.start();
			p.waitFor();
			System.out.println(pb.redirectInput());

			try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					System.out.println(line);
					String str=line.trim();
					if(str.startsWith("inet")) {
						str=str.split("inet")[1].split("/")[0].trim();
						if(!str.startsWith("127.0")) {
							
						}
						if(str.startsWith("10.0")) {
							addr=InetAddress.getByName(str);
							Constants.BROAD_CAST_ADDR="10.255.255.255";
						}
						if(str.startsWith("192.168.100")) {
							addr=InetAddress.getByName(str);
							Constants.BROAD_CAST_ADDR="192.168.100.255";
						}
						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public InetAddress getAddr() {
		return addr;
	}
	public DatagramSocket getDs() {
		return ds;
	}
	public void startReception() {
		rcv.loop = true;
		new Thread(rcv).start();
	}

	public void stopReception() {
		rcv.loop = false;
	}

	public InetAddress getLocalAddress() {
		return addr;
	}

	public void startSearchRoute(String address) {
		try {
			System.out.println("start search route");
			this.startSearchRoute(InetAddress.getByName(address));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startSearchRoute(InetAddress dest) {
		System.out.println("start search route inetaddress");
		try {
			DatagramPacket dp=pktmgr.generateInitialDatagramRequestPacket(this, dest);
			this.send(dp);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	public PacketManager<T> getPacketManager() {
		return pktmgr;
	}
	public void setSeqNum(int seq) {
		seqnum=seq;
	}
	public void incrementSeqNum() {
		seqnum++;
	}
	public int getSeqNum() {
		return seqnum;
	}
	public boolean checkSeqNum(int seq) {
		return seqnum<seq;
	}
	public void send(DatagramPacket dp) {
		try {
			System.out.println("node send to "+dp.getAddress().toString());
			ds.send(dp);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	protected T generateForwardingPacket(T pkt) {
		return pktmgr.generateForwardingPacket(pkt, this);
	}
	class Receiver implements Runnable {
		protected boolean loop;
		protected Node<T> node;
		
		public Receiver() {
			loop = false;
		}

		public void run() {
			try {
				DatagramPacket dp = new DatagramPacket(new byte[Constants.RCVBUFFER], Constants.RCVBUFFER);
				DatagramPacket forSend;
				while (loop) {
					System.out.println("wait for packets");
					ds.receive(dp);
					System.out.println("receive data");
					Constants.timer=System.currentTimeMillis()-Constants.timer;
					Constants.wait=false;
					System.out.println("received from "+dp.getAddress());
					if(dp.getAddress().getHostAddress().equals(addr.getHostAddress())) {
						System.out.println("received from self");
						continue;
					}
					forSend=pktmgr.received(dp,node);
					if(forSend!=null) {
						System.out.println("run -> send to "+forSend.getAddress()+" size "+forSend.getLength());
						node.send(forSend);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
