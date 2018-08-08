package ou.ist.de.srp.algo;

import java.util.HashMap;

import ou.ist.de.srp.packet.Packet;
import ou.ist.de.srp.packet.Packet;

public abstract class AbstractAlgorithm<T extends Packet> {
	
	protected HashMap<String,String> params;
	
	public AbstractAlgorithm(HashMap<String,String> params) {
		this.params=params;
	}
	public void setParameters(HashMap<String,String> params) {
		this.params=params;
	}
	public abstract byte[] sign(T pkt);
	public abstract boolean verify(T pkt);
}
