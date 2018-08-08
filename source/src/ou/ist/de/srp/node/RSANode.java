package ou.ist.de.srp.node;

import java.util.HashMap;

import ou.ist.de.srp.algo.rsa.RSAalgorithm;
import ou.ist.de.srp.packet.dsrbase.RSABasePacket;
import ou.ist.de.srp.packet.dsrbase.RSABasePacketManager;

public class RSANode extends Node<RSABasePacket> {

	public RSANode(HashMap<String, String> params) {
		super(params);
		// TODO Auto-generated constructor stub
	}
	public RSANode(HashMap<String, String> params,int port) {
		super(params,port);
		// TODO Auto-generated constructor stub
	}
	public RSANode(byte[] addr, HashMap<String, String> params) {
		super(addr, params);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected RSAalgorithm generateSignatureAlgorithm(HashMap<String, String> params) {
		// TODO Auto-generated method stub
		return new RSAalgorithm(params);
	}
	@Override
	protected RSABasePacketManager initializePacketManager(HashMap<String, String> params) {
		// TODO Auto-generated method stub
		return new RSABasePacketManager();
	}

}
