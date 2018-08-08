package ou.ist.de.srp.packet;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


public class FragmentPacketManager {
	
	protected int length;
	protected ConcurrentHashMap<String, ArrayList<FragmentPacket>> rcv;
	
	public FragmentPacketManager() {
		
		rcv=new ConcurrentHashMap<String,ArrayList<FragmentPacket>>();
	}
	public DatagramPacket[] fragmentation(Packet pkt,InetAddress next,int port) {
		
		byte[] data=pkt.toByteArray();
		byte[] fragmentData=null;
		ByteBuffer bb=ByteBuffer.wrap(data);
		
		int l=(data.length/length)+1;
		int cnt=0;
		DatagramPacket[] ret=new DatagramPacket[l];
		FragmentPacket fp=null;
		
		for(int i=0;i<l;i++) {
			cnt+=l;
			fp=new FragmentPacket();
			fp.src=pkt.src;
			fp.dst=pkt.dest;
			fp.seq=pkt.seq;
			fp.total=l;
			fp.index=i;
			fp.datalen=length;
			if(bb.remaining()>=l) {
				fp.data=new byte[l];
			}
			else {
				fp.data=new byte[bb.remaining()];
			}
			bb.get(fp.data);
			fragmentData=fp.toByteArray();
			ret[i]=new DatagramPacket(fragmentData, fragmentData.length, next, port);
		}
		return ret;
	}
	
	public Packet defragmentation(DatagramPacket dp) {
		FragmentPacket fp=new FragmentPacket();
		fp.fromByteArray(dp.getData());
		addReceivedPacket(fp);
		
		return null;
	}
	
	protected String genID(FragmentPacket fp) {
		return "id="+fp.src.toString()+"="+fp.dst.toString()+"="+fp.seq;
	}
	protected void addReceivedPacket(FragmentPacket fp) {
		
		ArrayList<FragmentPacket> al=null;
		String id=genID(fp);
		boolean found=false;;
		
		if(rcv.containsKey(id)) {
			al=rcv.get(id);
		}
		if(al==null) {
			al=new ArrayList<FragmentPacket>();
		}
		for(int i=0;i<al.size();i++) {
			found=fp.equalsTo(al.get(i));
		}
		if(!found) {	
			al.add(fp);
		}
		
		rcv.put(id, al);
	}
	public DatagramPacket receive() {
		
		return null;
	}
	public void send(DatagramPacket dp) {
		
	}
	
}

