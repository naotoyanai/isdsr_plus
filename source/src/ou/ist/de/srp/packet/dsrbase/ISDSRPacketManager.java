package ou.ist.de.srp.packet.dsrbase;

import ou.ist.de.srp.node.Node;
import ou.ist.de.srp.packet.PacketManager;

public class ISDSRPacketManager extends PacketManager<ISDSRPacket> {

	@Override
	protected ISDSRPacket addExtraInfoToInitialRequestPacket(ISDSRPacket pkt, Node<ISDSRPacket> node) {
		// TODO Auto-generated method stub
		pkt.ri.add(node.getLocalAddress());
		pkt.setSeq(node.getSeqNum());
		pkt.sig = alg.sign(pkt);
		return pkt;
	}

	@Override
	protected ISDSRPacket addExtraInfoToInitialReplyPacket(ISDSRPacket snd, ISDSRPacket rcv, Node<ISDSRPacket> node) {
		// TODO Auto-generated method stub
		snd.ri = rcv.ri;
		snd.ri.add(node.getLocalAddress());
		snd.setNext(snd.ri.get(snd.ri.getRiLength() - 1 - snd.getHops()));
		snd.sig = alg.sign(snd);
		return snd;
	}

	@Override
	protected ISDSRPacket addExtraInfoToForwardingRequestPacket(ISDSRPacket pkt, Node<ISDSRPacket> node) {
		// TODO Auto-generated method stub

		pkt.ri.add(node.getLocalAddress());
		pkt.setSig(alg.sign(pkt));
		return pkt;
	}

	@Override
	protected ISDSRPacket addExtraInfoToForwardingReplyPacket(ISDSRPacket pkt, Node<ISDSRPacket> node) {
		// TODO Auto-generated method stub

		pkt.setNext(pkt.ri.get(pkt.ri.getRiLength() - 1 - pkt.getHops()));
		return pkt;
	}

	@Override
	protected ISDSRPacket generatePlainPacket() {
		// TODO Auto-generated method stub
		return new ISDSRPacket();
	}

	@Override
	public boolean alreadyReceived(ISDSRPacket pkt, Node<ISDSRPacket> node) {
		// TODO Auto-generated method stub
		boolean ret = false;
		if (pkt.getRi().findNode(node.getLocalAddress()) && pkt.getType() == 0) {
			System.out.println("isdsr node interpret packet find itself in ri");
			return true;
		}
		ret = !node.checkSeqNum(pkt.getSeq());
		return ret;

	}

}
