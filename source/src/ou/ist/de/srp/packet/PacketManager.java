package ou.ist.de.srp.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

import ou.ist.de.srp.Constants;
import ou.ist.de.srp.algo.AbstractAlgorithm;
import ou.ist.de.srp.node.Node;

public abstract class PacketManager<T extends Packet> {

	protected AbstractAlgorithm<T> alg;
	protected boolean verifyBeforeForward;
	protected T prev;
	protected T next;
	protected boolean fragmentation;
	
	
	
	public T getPrev() {
		return prev;
	}

	public T getNext() {
		return next;
	}

	public void setVerification(boolean v) {
		this.verifyBeforeForward=v;
	}
	
	public void setFragmentation(boolean f) {
		this.fragmentation=f;
	}
	
	public DatagramPacket received(DatagramPacket dp,Node<T> node) {
		prev=generatePlainPacket();
		prev.fromByteArray(dp.getData());

		//if(alreadyReceived(prev,node)) {
		//	System.out.println("pkt="+prev.toString());
		//	System.out.println("received packets----------------------------------");
		//	return null;
		//}
		if(verifyBeforeForward) {
			if(!verifyPacket(prev)) {
				return null;
			}
		}
		next=generateForwardingPacket(prev,node);
		//System.out.println("packet manager forward "+forward);
		DatagramPacket dpforward=null;
		
		try {
			byte[] data=next.toByteArray();
			dpforward = new DatagramPacket(data, data.length, next.getNext(), Constants.PORT);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return dpforward;
		
	}
	protected abstract T addExtraInfoToInitialRequestPacket(T pkt, Node<T> node);

	protected abstract T addExtraInfoToInitialReplyPacket(T snd, T rcv, Node<T> node);

	protected abstract T addExtraInfoToForwardingRequestPacket(T pkt, Node<T> node);

	protected abstract T addExtraInfoToForwardingReplyPacket(T pkt, Node<T> node);

	protected abstract T generatePlainPacket();

	public abstract boolean alreadyReceived(T pkt, Node<T> node);
	
	public DatagramPacket generateInitialDatagramRequestPacket(Node<T> node,InetAddress dest) {
		T pkt=generateInitialRequestPacket(node,dest);
		byte[] data=pkt.toByteArray();
		DatagramPacket dp = new DatagramPacket(data, data.length, pkt.getNext(), Constants.PORT);
		return dp;
	}
	public void setAlgorithm(AbstractAlgorithm<T> algorithm) {
		alg = algorithm;
	}

	public T generateInitialRequestPacket(Node<T> node, InetAddress dest) {
		T pkt = generatePlainPacket();
		try {
			node.incrementSeqNum();
			pkt.src = node.getLocalAddress();
			pkt.dest = dest;
			pkt.hops = 1;
			pkt.sndr = pkt.src;
			pkt.next = InetAddress.getByName(Constants.BROAD_CAST_ADDR);
			pkt.type = Constants.REQ;
			addExtraInfoToInitialRequestPacket(pkt, node);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pkt;
	}

	public T generateInitialReplyPacket(T rcv, Node<T> node) {
		T pkt = generatePlainPacket();

		pkt.src = node.getLocalAddress();
		pkt.dest = rcv.src;
		pkt.sndr = node.getLocalAddress();
		pkt.type = Constants.REP;
		pkt.seq = rcv.seq;
		pkt.hops = 1;
		addExtraInfoToInitialReplyPacket(pkt, rcv, node);
		return pkt;
	}

	protected T checkRequestPacket(T pkt, Node<T> node) {
		T ret = null;
		// if(!node.checkSeqNum(pkt.getSeq())) {
		// return null;
		// }
		//System.out.println("check request packet "+pkt.toString());
		try {
			if (pkt.dest.equals(node.getLocalAddress())) {
				//if (node.checkSeqNum(pkt.seq)) {
					node.setSeqNum(pkt.seq);
					ret = generateInitialReplyPacket(pkt, node);
				//}
			} else {

				node.setSeqNum(pkt.seq);
				ret=generatePlainPacket();
				ret = pkt;
				ret.sndr = node.getLocalAddress();
				ret.next = InetAddress.getByName(Constants.BROAD_CAST_ADDR);
				ret.hops++;
				ret = addExtraInfoToForwardingRequestPacket(pkt, node);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	protected T checkReplyPacket(T pkt, Node<T> node) {
		System.out.println("received reply packet");
		T ret = null;
		try {
			if (pkt.dest.equals(node.getLocalAddress())) {
				System.out.println("received reply packet from " + pkt.getSrc());
			} else {
				ret = pkt;
				ret.hops++;
				ret.sndr = node.getLocalAddress();
				ret = addExtraInfoToForwardingReplyPacket(ret, node);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public T generateForwardingPacket(T pkt, Node<T> node) {

		switch (pkt.type) {
		case 0: {// REQ
			return checkRequestPacket(pkt, node);
		}
		case 1: {// REP
			//return checkReplyPacket(pkt, node);
			return null;
		}
		default: {

		}
		}
		return pkt;
	}

	public boolean verifyPacket(T pkt) {
		return alg.verify(pkt);
	}
	
}
