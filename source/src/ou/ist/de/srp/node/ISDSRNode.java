package ou.ist.de.srp.node;

import java.util.HashMap;

import ou.ist.de.srp.algo.AbstractAlgorithm;
import ou.ist.de.srp.algo.ibsas.IBSASalgorithm;
import ou.ist.de.srp.packet.PacketManager;
import ou.ist.de.srp.packet.dsrbase.ISDSRPacket;
import ou.ist.de.srp.packet.dsrbase.ISDSRPacketManager;

public class ISDSRNode extends Node<ISDSRPacket> {

	public ISDSRNode(byte[] addr, HashMap<String, String> params) {
		super(addr, params);
	}

	public ISDSRNode(HashMap<String, String> params, int port) {
		super(params, port);
		// TODO Auto-generated constructor stub
	}

	public ISDSRNode(HashMap<String, String> params) {
		super(params);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected AbstractAlgorithm<ISDSRPacket> generateSignatureAlgorithm(HashMap<String, String> params) {
		// TODO Auto-generated method stub
		params.put("uid", this.addr.getHostAddress());
		System.out.println("uid=" + this.addr.getHostAddress());
		
		return new IBSASalgorithm(params);
	}

	@Override
	protected PacketManager<ISDSRPacket> initializePacketManager(HashMap<String, String> params) {
		// TODO Auto-generated method stub
		return new ISDSRPacketManager();
	}

}
