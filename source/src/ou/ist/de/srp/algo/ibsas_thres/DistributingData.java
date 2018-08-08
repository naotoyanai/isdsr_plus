package ou.ist.de.srp.algo.ibsas_thres;

import it.unisa.dia.gas.jpbc.Element;

public class DistributingData {
	protected int from;
	protected int to;
	protected Element e;
	protected int type;
	
	public DistributingData() {
		
	}
	public DistributingData(int from,int to,int type,Element e) {
		this.from=from;
		this.to=to;
		this.type=type;
		this.e=e;
	}
}
